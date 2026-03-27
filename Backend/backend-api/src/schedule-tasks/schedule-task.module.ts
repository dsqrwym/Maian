import { Module } from '@nestjs/common';
import { CleanupTask } from './cleanup-task.service';
import { ScheduleModule } from '@nestjs/schedule';
import { CleanupFilesService } from './cleanup-files.services';
import { DistributedLockService } from './distributed-lock.service';

@Module({
  imports: [ScheduleModule.forRoot()], // 负责任务调度的 NestJS  cron 包集成模块
  providers: [CleanupTask, CleanupFilesService, DistributedLockService],
})
export class ScheduleTaskModule {}
