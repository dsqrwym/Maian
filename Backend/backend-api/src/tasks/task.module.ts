import { Module } from '@nestjs/common';
import { CleanupTask } from './cleanup.task';

@Module({
  providers: [CleanupTask],
})
export class TaskModule {}
