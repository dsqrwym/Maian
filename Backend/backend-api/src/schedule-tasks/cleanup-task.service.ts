import { Injectable } from '@nestjs/common';
import { PinoLogger } from 'nestjs-pino';
import { reduceDay } from '#/utils/date.utils.js';
import { Cron, CronExpression } from '@nestjs/schedule';
import { DistributedLockService } from './distributed-lock.service.js';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import {
  user_sessions,
  users,
  verification_tokens,
} from '#/generated/drizzle/schema.js';
import {
  and,
  eq,
  exists,
  gt,
  inArray,
  lt,
  not,
  notInArray,
  or,
  sql,
} from 'drizzle-orm';

const CLEANUP_TASK_LOCK_KEY = 'cron:cleanup:users';
const CLEANUP_TASK_LOCK_TTL_MS = 10 * 60 * 1000;

@Injectable()
export class CleanupTask {
  constructor(
    private readonly logger: PinoLogger,
    private readonly drizzleService: DrizzleService,
    private readonly distributedLockService: DistributedLockService,
  ) {
    this.logger.setContext(CleanupTask.name);
  }

  private async cleanupSessions(now: Date) {
    const deleteDate = reduceDay(now, 30);
    try {
      const sessions = await this.drizzleService.db
        .delete(user_sessions)
        .where(lt(user_sessions.last_active, deleteDate.toISOString()));
      this.logger.info(`Deleted ${sessions.rowCount ?? 0} user sessions.`);
    } catch (e) {
      this.logger.error('Failed to delete old sessions', e);
    }
  }

  private async cleanupVerificationTokens(now: Date) {
    const deleted = await this.drizzleService.db
      .delete(verification_tokens)
      .where(
        or(
          lt(verification_tokens.expires_at, now.toISOString()),
          eq(verification_tokens.is_used, true),
        ),
      );
    this.logger.info(`Deleted: ${deleted.rowCount ?? 0} verification tokens.`);
  }

  private async cleanupUnverifiedUsers(now: Date) {
    const deleteDate = reduceDay(now, 1);
    const deleted = await this.drizzleService.db
      .delete(users)
      .where(
        and(
          eq(users.status, 'PENDING_VERIFICATION'),
          notInArray(users.role, ['SUPPORT', 'DELIVERY', 'WAREHOUSE', 'ADMIN']),
          lt(users.created_at, deleteDate.toISOString()),
        ),
      );

    this.logger.info(`Deleted: ${deleted.rowCount ?? 0} users.`);
  }

  private async cleanupIncompleteInformationUsers(now: Date) {
    const deleteDate = reduceDay(now, 7);
    const deleted = await this.drizzleService.db.delete(users).where(
      or(
        and(
          eq(users.status, 'INACTIVE'),
          lt(users.created_at, deleteDate.toISOString()),
        ),
        // 员工类用户：待验证且属于特定角色
        and(
          eq(users.status, 'PENDING_VERIFICATION'),
          inArray(users.role, ['SUPPORT', 'DELIVERY', 'WAREHOUSE']),
          lt(users.created_at, deleteDate.toISOString()),
          not(
            exists(
              this.drizzleService.db
                .select({ one: sql`1` })
                .from(verification_tokens)
                .where(
                  and(
                    eq(verification_tokens.user_id, users.id),
                    eq(verification_tokens.is_used, false),
                    gt(verification_tokens.expires_at, now.toISOString()),
                  ),
                ),
            ),
          ),
        ),
      ),
    );
    this.logger.info(`Deleted: ${deleted.rowCount ?? 0} users.`);
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
