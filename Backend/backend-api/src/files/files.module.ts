import { Global, Module } from '@nestjs/common';
import { FilesService } from './files.service.js';
import { FilesController } from './files.controller.js';
import { FileVideoPlayTokenService } from './services/file-video-play-token.service.js';
import { STORAGE_DRIVER } from './storage/storage-key.js';
import { LocalStorageDriver } from './storage/local-storage.driver.js';
import { CloudflareStorageDriver } from './storage/cloudflare-storage.driver.js';
import { SyncStorageDriver } from './storage/sync-storage.driver.js';
import { ConfigService } from '@nestjs/config';
import { ENV } from '#/config/constants.config.js';
import { DrizzleModule } from '#/drizzle/drizzle.module.js';
import { CommonModule } from '#/common/common.module.js';
import { PinoLogger } from 'nestjs-pino';
import { JwtModule } from '@nestjs/jwt';

@Global()
@Module({
  controllers: [FilesController],
  imports: [DrizzleModule, CommonModule, JwtModule],
  providers: [
    FilesService,
    FileVideoPlayTokenService,
    LocalStorageDriver,
    CloudflareStorageDriver,
    SyncStorageDriver,
    {
      provide: STORAGE_DRIVER,
      inject: [
        ConfigService,
        LocalStorageDriver,
        CloudflareStorageDriver,
        SyncStorageDriver,
        PinoLogger,
      ],
      useFactory: (
        config: ConfigService,
        local: LocalStorageDriver,
        cloudflare: CloudflareStorageDriver,
        sync: SyncStorageDriver,
        logger: PinoLogger,
      ) => {
        const endpoint = config.get<string>(ENV.R2_ENDPOINT);
        const secret = config.get<string>(ENV.R2_SECRET_ACCESS_KEY);
        const accessKey = config.get<string>(ENV.R2_ACCESS_KEY_ID);
        const isCloudConfigured = !!(endpoint && secret && accessKey);

        const syncEnabled =
          config.get<string>(ENV.FILE_SYNC_ENABLED) === 'true';

        // 如果开启了同步且云端配置有效，使用同步驱动
        if (syncEnabled && isCloudConfigured) {
          logger.info('Using SyncStorageDriver (sync mode enabled)');
          return sync;
        }

        // 如果云端配置有效，使用云端驱动
        if (isCloudConfigured) {
          logger.info('Using CloudflareStorageDriver (cloud only mode)');
          return cloudflare;
        }

        // 否则使用本地驱动
        logger.info('Using LocalStorageDriver (local only mode)');
        return local;
      },
    },
  ],
  exports: [
    FilesService,
    FileVideoPlayTokenService,
    STORAGE_DRIVER,
    LocalStorageDriver,
    CloudflareStorageDriver,
    SyncStorageDriver,
  ],
})
export class FilesModule {}
