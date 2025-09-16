import { Module } from '@nestjs/common';
import { CleanupTask } from './cleanup-task.service';
import { ScheduleModule } from '@nestjs/schedule';
import { EmailTasksService } from './email-tasks.service';

@Module({
  imports: [ScheduleModule.forRoot()], // 负责任务调度的 NestJS  cron 包集成模块
  providers: [CleanupTask, EmailTasksService],
})
export class ScheduleTaskModule {}
