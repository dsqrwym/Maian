import { Injectable } from '@nestjs/common';
import { Cron, CronExpression } from '@nestjs/schedule';
import { Logger } from 'nestjs-pino';
import { PrismaService } from 'src/prisma/prisma.service';
import { UserStatus } from '../../prisma/generated';
import { reduceDay } from '../utils/date.utils';

@Injectable()
export class CleanupTask {
  constructor(
    private readonly prismaService: PrismaService,
    private readonly logger: Logger,
  ) {}

  private async cleanupSessions(now: Date) {
    const deleteDate = reduceDay(now, 30);
    try {
      const sessions = await this.prismaService.user_sessions.deleteMany({
        where: { last_active: { lt: deleteDate } },
      });
      this.logger.log(`Deleted ${sessions.count} user sessions.`);
    } catch (e) {
      this.logger.error('Failed to delete old sessions', e);
    }
  }

  private async cleanupVerificationTokens(now: Date) {
    const deleted = await this.prismaService.verification_tokens.deleteMany({
      where: {
        expires_at: { lt: now },
        OR: [{ is_used: true }],
      },
    });
    this.logger.log(`Deleted: ${deleted.count} verification tokens.`);
  }

  private async cleanupUnverifiedUsers(now: Date) {
    const deleteDate = reduceDay(now, 1);
    const deleted = await this.prismaService.users.deleteMany({
      where: {
        status: UserStatus.PENDING_VERIFICATION,
        AND: {
          created_at: { lt: deleteDate },
        },
      },
    });
    this.logger.log(`Deleted: ${deleted.count} users.`);
  }

  private async cleanupIncompleteInformationUsers(now: Date) {
    const deleteDate = reduceDay(now, 7);
    const deleted = await this.prismaService.users.deleteMany({
      where: {
        status: UserStatus.INACTIVE,
        AND: {
          created_at: { lt: deleteDate },
        },
      },
    });
    this.logger.log(`Deleted: ${deleted.count} users.`);
  }

  @Cron(CronExpression.EVERY_DAY_AT_3AM)
  async handleCleanup() {
    const now = new Date();
    // 清理 会话
    await this.cleanupSessions(now);
    // 清理 验证token
    await this.cleanupVerificationTokens(now);

    // 删除用户
    await this.cleanupUnverifiedUsers(now);
    // 删除未填充信息的用户
    await this.cleanupIncompleteInformationUsers(now);
  }

  @Cron(CronExpression.EVERY_DAY_AT_5AM)
  async handleFileCleanup() {
    // 删除文件地址储存以及文件（谷歌API）
    const deleted = await this.prismaService.files.deleteMany({
      where: {
        AND: [
          { message_files: { none: {} } },
          { products_files: { none: {} } },
          // 将来新增引用表继续添加
        ],
      },
    });
    this.logger.log(`Cleaned up ${deleted.count} unreferenced files.`);
  }
}
