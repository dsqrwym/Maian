import { Global, Module } from '@nestjs/common';
import { seconds, ThrottlerModule } from '@nestjs/throttler';
import { ConfigService } from '@nestjs/config';
import { ENV } from '../../config/constants.config';
import { APP_GUARD } from '@nestjs/core';
import { CustomThrottlerGuard } from '../guards/custom-throttler.guard';
import { ThrottlerStorageRedisService } from '@nest-lab/throttler-storage-redis';
import { IoRedisService } from '../../cache/redis/ioredis.cache.service';

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
      inject: [ConfigService, IoRedisService],
      useFactory: (
        configService: ConfigService,
        ioRedisService: IoRedisService,
      ) => ({
        throttlers: [
          {
            ttl: seconds(configService.get<number>(ENV.THROTTLER_TTL, 60)),
            limit: configService.get<number>(ENV.THROTTLER_LIMIT, 100),
          },
        ],
        storage: new ThrottlerStorageRedisService(ioRedisService.getClient()),
      }),
    }),
  ],
})
export class MyThrottlerModule {}
