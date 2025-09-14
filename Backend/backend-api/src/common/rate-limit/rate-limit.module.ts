import { Module } from '@nestjs/common';
import { seconds, ThrottlerModule } from '@nestjs/throttler';
import { ConfigService } from '@nestjs/config';
import { ENV } from '../../config/constants.config';
import { APP_GUARD } from '@nestjs/core';
import { CustomThrottlerGuard } from '../guards/custom-throttler.guard';
import { ThrottlerStorageRedisService } from '@nest-lab/throttler-storage-redis';

@Module({
  providers: [
    {
      provide: APP_GUARD,
      useClass: CustomThrottlerGuard,
    },
  ],

  imports: [
    ThrottlerModule.forRootAsync({
      inject: [ConfigService],
      useFactory: (configService: ConfigService) => ({
        throttlers: [
          {
            ttl: seconds(configService.get<number>(ENV.THROTTLER_TTL, 60)),
            limit: configService.get<number>(ENV.THROTTLER_LIMIT, 100),
          },
        ],
        storage: new ThrottlerStorageRedisService(
          configService.get<string>(
            ENV.REDIS_CACHE_URL,
            'redis://localhost:6379',
          ),
        ),
      }),
    }),
  ],
})
export class MyThrottlerModule {}
