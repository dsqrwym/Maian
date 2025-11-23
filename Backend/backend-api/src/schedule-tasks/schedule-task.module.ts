import { Module } from '@nestjs/common';
import { CleanupTask } from './cleanup-task.service';
import { ScheduleModule } from '@nestjs/schedule';
import { CleanupFilesService } from './cleanup-files.services';

@Module({
  imports: [ScheduleModule.forRoot()], // 负责任务调度的 NestJS  cron 包集成模块
  providers: [CleanupTask, CleanupFilesService],
})
export class ScheduleTaskModule {}
