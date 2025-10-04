import { Injectable, Inject } from '@nestjs/common';
import { CacheInterceptor } from '@nestjs/cache-manager';
import type { Cache } from 'cache-manager';
import { REDIS_CACHE } from './cache.redis.token';
import { Reflector } from '@nestjs/core';

@Injectable()
export class RedisCacheInterceptor extends CacheInterceptor {
  constructor(@Inject(REDIS_CACHE) cacheManager: Cache, reflector: Reflector) {
    super(cacheManager, reflector);
  }
}
