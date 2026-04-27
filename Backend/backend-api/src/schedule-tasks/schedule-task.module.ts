import { Module } from '@nestjs/common';
import { CleanupTask } from './cleanup-task.service.js';
import { ScheduleModule } from '@nestjs/schedule';
import { CleanupFilesService } from './cleanup-files.services.js';
import { DistributedLockService } from './distributed-lock.service.js';
import { FilesModule } from '#/files/files.module.js';

@Module({
  imports: [ScheduleModule.forRoot(), FilesModule], // 负责任务调度的 NestJS  cron 包集成模块
  providers: [CleanupTask, CleanupFilesService, DistributedLockService],
})
export class ScheduleTaskModule {}
