import { Injectable } from "@nestjs/common";
import { Cron, CronExpression } from "@nestjs/schedule";
import { Logger } from "nestjs-pino";
import { DateFormatService } from "src/common/services/date-format.service";
import { PrismaService } from "src/prisma/prisma.service";
import { SupabaseService } from "src/supabase/supabase.service";

@Injectable()
export class CleanupTask {
    constructor(
        private readonly supabaseService: SupabaseService,
        private readonly prismaService: PrismaService,
        private readonly dateService: DateFormatService,
        private readonly logger: Logger
    ) { }


    private async cleanupSessions(now: Date) {
        const deleteDate = this.dateService.reduceDay(now, 30);
        try {
            const sessions = await this.prismaService.user_sessions.deleteMany({
                where: { last_active: { lt: deleteDate } }
            });
            this.logger.log(`Deleted ${sessions.count} user sessions.`);
        } catch (e) {
            this.logger.error('Failed to delete old sessions', e);
        }
    }

    private async setUnverifiedUsersState(now: Date) {
        try {
            const updated = await this.prismaService.public_users.updateMany({
                where: {
                    AND: {
                        status: 0,
                        created_at: { lt: this.dateService.reduceDay(now, 3) }
                    }
                },
                data: { status: -1 }
            });
            this.logger.log(`Marked ${updated.count} users as inactive.`);
        } catch (e) {
            this.logger.error('Failed to update user status', e);
        }
    }

    private async cleanupUnverifiedUsers(now: Date) {
        try {
            const { error } = await this.supabaseService.getClient().from('users')
                .delete()
                .match({ status: -1 })
                .lt('created_at', this.dateService.reduceDay(now, 7));
            if (error) throw error;
            this.logger.log(`Deleted inactive users from Supabase.`);
        } catch (e) {
            this.logger.error('Failed to delete users from Supabase', e);
        }
    }

    @Cron(CronExpression.EVERY_DAY_AT_3AM)
    async handleCleanup() {
        const now = new Date();
        // 清理 会话
        this.cleanupSessions(now);

        // 设置 用户为限制状态 -1
        this.setUnverifiedUsersState(now);

        // 删除用户
        this.cleanupUnverifiedUsers(now);
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
                ]
            }
        });
        this.logger.log(`Cleaned up ${deleted.count} unreferenced files.`);
    }
}