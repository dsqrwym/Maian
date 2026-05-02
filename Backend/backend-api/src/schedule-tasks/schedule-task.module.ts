import { Module } from '@nestjs/common';
import { CleanupTask } from './cleanup-task.service.js';
import { ScheduleModule } from '@nestjs/schedule';
import { CleanupFilesService } from './cleanup-files.services.js';
import { DistributedLockService } from './distributed-lock.service.js';
import { FileSyncService } from './file-sync.service.js';
import { FilesModule } from '#/files/files.module.js';
import { DrizzleModule } from '#/drizzle/drizzle.module.js';

@Module({
  imports: [ScheduleModule.forRoot(), FilesModule], // 负责任务调度的 NestJS  cron 包集成模块
  providers: [
    CleanupTask,
    CleanupFilesService,
    DistributedLockService,
    FileSyncService,
    DrizzleModule,
  ],
})
export class ScheduleTaskModule {}
