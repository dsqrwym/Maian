import { Global, Module } from '@nestjs/common';
import { seconds, ThrottlerModule } from '@nestjs/throttler';
import { ConfigService } from '@nestjs/config';
import { ENV } from '#/config/constants.config.js';
import { APP_GUARD } from '@nestjs/core';
import { CustomThrottlerGuard } from '../guards/custom-throttler.guard.js';
import { ThrottlerStorageRedisService } from '@nest-lab/throttler-storage-redis';
import { IoRedisService } from '#/cache/redis/ioredis.cache.service.js';
import { CacheRedisModule } from '#/cache/cache.redis.module.js';

@Global()
@Module({
  providers: [
    {
      provide: APP_GUARD,
      useClass: CustomThrottlerGuard,
    },
  ],

  imports: [
    ThrottlerModule.forRootAsync({
      imports: [CacheRedisModule],
      inject: [ConfigService, IoRedisService],
      useFactory: (
        configService: ConfigService,
        ioRedisService: IoRedisService,
      ) => ({
        throttlers: [
          {
            ttl: seconds(
              Number(configService.get<number>(ENV.THROTTLER_TTL, 60)),
            ),
            limit: Number(configService.get<number>(ENV.THROTTLER_LIMIT, 100)),
          },
        ],
        storage: new ThrottlerStorageRedisService(ioRedisService.getClient()),
      }),
    }),
  ],
})
export class MyThrottlerModule {}
