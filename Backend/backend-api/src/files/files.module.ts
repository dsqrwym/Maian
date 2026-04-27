import { Global, Module } from '@nestjs/common';
import { FilesService } from './files.service.js';
import { FilesController } from './files.controller.js';
import { STORAGE_DRIVER } from './storage/storage-key.js';
import { LocalStorageDriver } from './storage/local-storage.driver.js';
import { CloudflareStorageDriver } from './storage/cloudflare-storage.driver.js';
import { ConfigService } from '@nestjs/config';
import { ENV } from '#/config/constants.config.js';

@Global()
@Module({
  controllers: [FilesController],
  providers: [
    FilesService,
    LocalStorageDriver,
    CloudflareStorageDriver,
    {
      provide: STORAGE_DRIVER,
      inject: [ConfigService, LocalStorageDriver, CloudflareStorageDriver],
      useFactory: (
        config: ConfigService,
        local: LocalStorageDriver,
        cloudflare: CloudflareStorageDriver,
      ) => {
        const endpoint = config.get<string>(ENV.R2_ENDPOINT);
        const secret = config.get<string>(ENV.R2_SECRET_ACCESS_KEY);
        const accessKey = config.get<string>(ENV.R2_ACCESS_KEY_ID);
        const isCloudFlareConfigValid = !!(endpoint && secret && accessKey);

        return isCloudFlareConfigValid ? cloudflare : local;
      },
    },
  ],
  exports: [FilesService, STORAGE_DRIVER, LocalStorageDriver],
})
export class FilesModule {}
