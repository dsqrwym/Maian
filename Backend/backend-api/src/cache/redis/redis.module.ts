import { Global, Module } from '@nestjs/common';
import { createKeyv } from '@keyv/redis';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { PinoLogger } from 'nestjs-pino';
import { ENV } from '../../config/constants';
import Keyv from 'keyv';

export const REDIS_CACHE = 'REDIS_CACHE';

@Global()
@Module({
  imports: [ConfigModule],
  providers: [
    {
      provide: REDIS_CACHE,
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
        }

        logger.info(
          { subsystem: 'redis', event: 'init' },
          'Initializing KeyvRedis',
        );

        let keyv: Keyv;
        try {
          keyv = createKeyv(redisUrl, {
            namespace: 'redis-cache',
          });

          // Redis 错误监听
          keyv.on('error', (err: unknown) => {
            logger.error({ err }, 'Redis Keyv Error, falling back to memory');
          });
        } catch (err: unknown) {
          logger.warn({ err }, 'Redis unavailable, using in-memory cache');
          keyv = new Keyv({ namespace: 'redis-cache' }); // 内存兜底
        }

        return keyv;
      },
      inject: [ConfigService, PinoLogger],
    },
  ],
  exports: [REDIS_CACHE],
})
export class RedisCacheModule {}
