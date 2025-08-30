import { Global, Module } from '@nestjs/common';
import * as redisStore from 'cache-manager-redis-store';
import { CACHE_MANAGER, CacheModule } from '@nestjs/cache-manager';
import { ConfigModule, ConfigService } from '@nestjs/config';

export const REDIS_CACHE = 'REDIS_CACHE';

@Global()
@Module({
  imports: [
    ConfigModule, // 确保 ConfigService 可用
    CacheModule.registerAsync({
      useFactory: (configService: ConfigService) => ({
        store: redisStore,
        host: configService.get<string>('REDIS_HOST') || '127.0.0.1',
        port: configService.get<number>('REDIS_PORT') || 6379,
        ttl: configService.get<number>('REDIS_TTL', 500),
        password: configService.get<string>('REDIS_PASSWORD') || undefined,
      }),
      inject: [ConfigService],
    }),
  ],
  providers: [
    {
      provide: REDIS_CACHE,
      useFactory: (cacheManager: Cache): Cache => {
        return cacheManager; // 显式返回 Cache 类型
      },
      inject: [CACHE_MANAGER], // Nest 自带 token
    },
  ],
  exports: [REDIS_CACHE],
})
export class RedisCacheModule {}
