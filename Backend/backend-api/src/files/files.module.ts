import { Module } from '@nestjs/common';
import { FilesService } from './files.service';
import { FilesController } from './files.controller';
import { STORAGE_DRIVER } from './storage/storage-key';
import { LocalStorageDriver } from './storage/local-storage.driver';

@Module({
  controllers: [FilesController],
  providers: [
    FilesService,
    {
      provide: STORAGE_DRIVER,
      useClass: LocalStorageDriver,
    },
  ],
})
export class FilesModule {}
