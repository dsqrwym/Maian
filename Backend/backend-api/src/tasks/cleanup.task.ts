import { Injectable } from '@nestjs/common';
import { Cron, CronExpression } from '@nestjs/schedule';
import { Logger } from 'nestjs-pino';
import { DateFormatService } from 'src/common/formatter/date-format.service';
import { PrismaService } from 'src/prisma/prisma.service';
import { UserStatus } from '../../prisma/generated/prisma';

@Injectable()
export class CleanupTask {
  constructor(
    private readonly prismaService: PrismaService,
    private readonly dateService: DateFormatService,
    private readonly logger: Logger,
  ) {}

  private async cleanupSessions(now: Date) {
    const deleteDate = this.dateService.reduceDay(now, 30);
    try {
      const sessions = await this.prismaService.user_sessions.deleteMany({
        where: { last_active: { lt: deleteDate } },
      });
      this.logger.log(`Deleted ${sessions.count} user sessions.`);
    } catch (e) {
      this.logger.error('Failed to delete old sessions', e);
    }
  }

  private async setUnverifiedUsersState(now: Date) {
    try {
      const updated = await this.prismaService.users.updateMany({
        where: {
          AND: {
            status: UserStatus.INACTIVE,
            created_at: { lt: this.dateService.reduceDay(now, 3) },
          },
        },
        data: { status: UserStatus.ACTIVE },
      });
      this.logger.log(`Marked ${updated.count} users as inactive.`);
    } catch (e) {
      this.logger.error('Failed to update user status', e);
    }
  }

  private async cleanupUnverifiedUsers(now: Date) {
    const deleted = await this.prismaService.users.deleteMany({
      where: {
        status: UserStatus.INACTIVE,
        AND: {
          created_at: { lt: this.dateService.reduceDay(now, 7) },
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

    // 设置 用户为限制状态 -1
    await this.setUnverifiedUsersState(now);

    // 删除用户
    await this.cleanupUnverifiedUsers(now);
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
