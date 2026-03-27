import { Inject, Injectable } from '@nestjs/common';
import * as fs from 'fs';
import * as path from 'path';
import { HOUR, reduceHours } from '../utils/date.utils';
import { STORAGE_DRIVER } from '../files/storage/storage-key';
import { LocalStorageDriver } from '../files/storage/local-storage.driver';
import { PrismaService } from '../prisma/prisma.service';
import { PinoLogger } from 'nestjs-pino';
import { Cron, CronExpression } from '@nestjs/schedule';
import { StorageDriver } from '../files/storage/storage.driver';
import { DistributedLockService } from './distributed-lock.service';

const CLEAN_TEMP_FILES_LOCK_KEY = 'cron:cleanup:temp-files';
const CLEAN_TEMP_FILES_LOCK_TTL_MS = 5 * 60 * 1000;
const MARK_FILES_LOCK_KEY = 'cron:cleanup:mark-files';
const MARK_FILES_LOCK_TTL_MS = 10 * 60 * 1000;
const DELETE_FILES_LOCK_KEY = 'cron:cleanup:delete-files';
const DELETE_FILES_LOCK_TTL_MS = 30 * 60 * 1000;

@Injectable()
export class CleanupFilesService {
  constructor(
    private readonly localDriver: LocalStorageDriver,
    @Inject(STORAGE_DRIVER) private readonly storage: StorageDriver,
    private readonly prismaService: PrismaService,
    private readonly logger: PinoLogger,
    private readonly distributedLockService: DistributedLockService,
  ) {
    this.logger.setContext(CleanupFilesService.name);
  }

  @Cron(CronExpression.EVERY_5_MINUTES)
  async cleanOldTempFiles() {
    await this.distributedLockService.runWithLock(
      CLEAN_TEMP_FILES_LOCK_KEY,
      CLEAN_TEMP_FILES_LOCK_TTL_MS,
      async () => {
        const files = await fs.promises.readdir(this.localDriver.getTempDir());
        const now = Date.now();
        const tasks: Promise<void>[] = [];

        for (const file of files) {
          const full = path.join(this.localDriver.getTempDir(), file);
          const stat = await fs.promises.stat(full);

          if (now - stat.mtimeMs > 6 * HOUR) {
            tasks.push(fs.promises.unlink(full).catch(() => {}));
          }
        }

        await Promise.allSettled(tasks);
      },
    );
  }

  @Cron(CronExpression.EVERY_HOUR)
  async markFileToCleanup() {
    await this.distributedLockService.runWithLock(
      MARK_FILES_LOCK_KEY,
      MARK_FILES_LOCK_TTL_MS,
      async () => {
        const deleteDate = reduceHours(new Date(), 6);
        const deleted = await this.prismaService.files.updateMany({
          where: {
            AND: [
              { to_delete: false },
              { message_files: { none: {} } },
              { products_files: { none: {} } },
              { created_at: { lt: deleteDate } },
              // 将来新增引用表继续添加
            ],
          },
          data: { to_delete: true },
        });
        this.logger.info(
          `Mark up ${deleted.count} unreferenced files to delete.`,
        );
      },
    );
  }

  @Cron('0 */15 * * * *') // 每 15 分钟执行一次)
  async handleFileCleanup() {
    await this.distributedLockService.runWithLock(
      DELETE_FILES_LOCK_KEY,
      DELETE_FILES_LOCK_TTL_MS,
      async () => {
        const deleteDate = reduceHours(new Date(), 6);
        const batchSize = 500; // 每次从 DB 拉取多少条
        const concurrency = 8; // 并发 storage.delete 的限制
        let totalDeleted = 0;
        let totalFailed = 0;
        let lastId: number | null = null;

        while (true) {
          // 分页拉取（按 id 升序），避免一次拉出过多
          const files = await this.prismaService.files.findMany({
            where: {
              to_delete: true,
              created_at: { lt: deleteDate },
              ...(lastId && { id: { gt: lastId } }),
            },
            orderBy: { id: 'asc' },
            take: batchSize,
          });

          // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
          if (files.length === 0) break;

          // 并发限流执行 storage.delete，收集成功或可忽略的 id
          const idsToDeleteFromDb: bigint[] = [];
          let failedThisBatch = 0;

          // helper to run limited concurrency without extra deps
          const runners: Promise<void>[] = [];
          let idx = 0;
          const runNext = async () => {
            // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
            while (idx < files.length) {
              // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment,@typescript-eslint/no-unsafe-member-access
              const current = files[idx++];
              try {
                // storage.delete 可能抛错 --- 处理 ENOENT 为可忽略
                await this.storage
                  // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access,@typescript-eslint/no-unsafe-argument
                  .delete(current.storage_key)
                  .catch((err: unknown) => {
                    const e = err as
                      | NodeJS.ErrnoException
                      | { code?: string; message?: string };
                    if (e && (e.code === 'ENOENT' || e.code === 'NotFound')) {
                      this.logger.debug(
                        // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
                        `Storage missing for ${current.storage_key}, will remove DB record.`,
                      );
                      return;
                    }
                    throw err;
                  });

                // 如果 storage.delete 成功或文件本就不存在，加入批量删除名单
                // eslint-disable-next-line @typescript-eslint/no-unsafe-argument,@typescript-eslint/no-unsafe-member-access
                idsToDeleteFromDb.push(current.id);
              } catch (err) {
                failedThisBatch++;
                this.logger.error(
                  { err },
                  // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
                  `Failed to delete storage for ${current.storage_key}`,
                );
                // 不把这个 id 加入 idsToDeleteFromDb，这样不会删除 DB（保留做重试）
              }
            }
          };

          // 启动 concurrency 个 runner
          for (let i = 0; i < concurrency; i++) {
            runners.push(runNext());
          }
          await Promise.allSettled(runners);

          // 批量从数据库中删除成功删除了存储的那些记录
          if (idsToDeleteFromDb.length > 0) {
            const deleteResult = await this.prismaService.files.deleteMany({
              where: {
                id: { in: idsToDeleteFromDb },
                to_delete: true,
                created_at: { lt: deleteDate }, // 再次 double-check
              },
            });
            totalDeleted += deleteResult.count;
          }

          totalFailed += failedThisBatch;

          // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment,@typescript-eslint/no-unsafe-member-access
          lastId = files[files.length - 1].id;
        }

        this.logger.info(
          `Deleted ${totalDeleted} files from DB. Failed storage deletions: ${totalFailed}`,
        );
      },
    );
  }
}
