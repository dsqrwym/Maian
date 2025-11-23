import { Global, Module } from '@nestjs/common';
import { FilesService } from './files.service';
import { FilesController } from './files.controller';
import { STORAGE_DRIVER } from './storage/storage-key';
import { LocalStorageDriver } from './storage/local-storage.driver';

@Global()
@Module({
  controllers: [FilesController],
  providers: [
    FilesService,
    {
      provide: STORAGE_DRIVER,
      useClass: LocalStorageDriver,
    },
  ],
  exports: [FilesService, STORAGE_DRIVER],
})
export class FilesModule {}
