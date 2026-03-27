import { Injectable } from '@nestjs/common';
import { PinoLogger } from 'nestjs-pino';
import { PrismaService } from 'src/prisma/prisma.service';
import { UserRole, UserStatus } from 'src/generated/prisma/client';
import { reduceDay } from '../utils/date.utils';
import { Cron, CronExpression } from '@nestjs/schedule';
import { DistributedLockService } from './distributed-lock.service';

const CLEANUP_TASK_LOCK_KEY = 'cron:cleanup:users';
const CLEANUP_TASK_LOCK_TTL_MS = 10 * 60 * 1000;

@Injectable()
export class CleanupTask {
  constructor(
    private readonly prismaService: PrismaService,
    private readonly logger: PinoLogger,
    private readonly distributedLockService: DistributedLockService,
  ) {
    this.logger.setContext(CleanupTask.name);
  }

  private async cleanupSessions(now: Date) {
    const deleteDate = reduceDay(now, 30);
    try {
      const sessions = await this.prismaService.user_sessions.deleteMany({
        where: { last_active: { lt: deleteDate } },
      });
      this.logger.info(`Deleted ${sessions.count} user sessions.`);
    } catch (e) {
      this.logger.error('Failed to delete old sessions', e);
    }
  }

  private async cleanupVerificationTokens(now: Date) {
    const deleted = await this.prismaService.verification_tokens.deleteMany({
      where: {
        OR: [{ expires_at: { lt: now } }, { is_used: true }]
      },
    });
    this.logger.info(`Deleted: ${deleted.count} verification tokens.`);
  }

  private async cleanupUnverifiedUsers(now: Date) {
    const deleteDate = reduceDay(now, 1);
    const deleted = await this.prismaService.users.deleteMany({
      where: {
        status: UserStatus.PENDING_VERIFICATION,
        role: {
          notIn: [
            UserRole.SUPPORT,
            UserRole.DELIVERY,
            UserRole.WAREHOUSE,
            UserRole.ADMIN,
          ],
        },
        AND: {
          created_at: { lt: deleteDate },
        },
      },
    });
    this.logger.info(`Deleted: ${deleted.count} users.`);
  }

  private async cleanupIncompleteInformationUsers(now: Date) {
    const deleteDate = reduceDay(now, 7);
    const deleted = await this.prismaService.users.deleteMany({
      where: {
        OR: [
          { status: UserStatus.INACTIVE, created_at: { lt: deleteDate } },
          // 员工类用户：待验证且属于特定角色
          {
            status: UserStatus.PENDING_VERIFICATION,
            role: {
              in: [UserRole.SUPPORT, UserRole.DELIVERY, UserRole.WAREHOUSE],
            },
            created_at: { lt: deleteDate },
          },
        ],
      },
    });
    this.logger.info(`Deleted: ${deleted.count} users.`);
  }

  @Cron(CronExpression.EVERY_HOUR)
  async handleCleanup() {
    await this.distributedLockService.runWithLock(
      CLEANUP_TASK_LOCK_KEY,
      CLEANUP_TASK_LOCK_TTL_MS,
      async () => {
        const now = new Date();
        // 清理 会话
        await this.cleanupSessions(now);
        // 清理 验证token
        await this.cleanupVerificationTokens(now);

        // 删除用户
        await this.cleanupUnverifiedUsers(now);
        // 删除未填充信息的用户
        await this.cleanupIncompleteInformationUsers(now);
      },
    );
  }
}
