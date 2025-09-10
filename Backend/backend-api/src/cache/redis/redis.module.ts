import { Global, Module } from '@nestjs/common';
import KeyvRedis from '@keyv/redis';
import Keyv from 'keyv';
import { CACHE_MANAGER, CacheModule } from '@nestjs/cache-manager';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { ENV } from '../config/constants';
import { PinoLogger } from 'nestjs-pino';

export const REDIS_CACHE = 'REDIS_CACHE';

@Global()
@Module({
  imports: [
    ConfigModule, // 确保 ConfigService 可用
    CacheModule.registerAsync({
      useFactory: (configService: ConfigService, logger: PinoLogger) => {
        let redisUrl = configService.get<string>(ENV.REDIS_URL);
        if (!redisUrl) {
          const host = configService.get<string>(ENV.REDIS_HOST, '127.0.0.1');
          const port = Number(configService.get<number>(ENV.REDIS_PORT, 6379));
          const username = configService.get<string>(
            ENV.REDIS_USERNAME,
            'default',
          );
          const password = configService.get<string>(ENV.REDIS_PASSWORD);
          redisUrl = password
            ? `redis://${username}:${password}@${host}:${port}`
            : `redis://${host}:${port}`;
          logger.info(
            { subsystem: 'redis', host, port, event: 'init' },
            'Redis: initializing KeyvRedis store',
          );
        }
        logger.info(
          { subsystem: 'redis', event: 'init' },
          'Redis: initializing KeyvRedis store with Redis Url',
        );

        return {
          ttl: configService.get<number>(ENV.REDIS_TTL, 5000),
          store: new Keyv({ store: new KeyvRedis(redisUrl) }),
        };
      },
      inject: [ConfigService, PinoLogger],
    }),
  ],
  providers: [
    {
      provide: REDIS_CACHE,
      /*useFactory: (cacheManager: Cache): Cache => {
        return cacheManager; // 显式返回 Cache 类型
      }*/
      useExisting: CACHE_MANAGER,
    },
  ],
  exports: [REDIS_CACHE],
})
export class RedisCacheModule {}
