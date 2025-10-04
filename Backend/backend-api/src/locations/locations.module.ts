import { Module } from '@nestjs/common';
import { LocationsController } from './locations.controller';
import { LocationsService } from './locations.service';
import { CacheRedisModule } from '../cache/cache.redis.module';
import { REDIS_CACHE } from '../cache/redis/cache.redis.token';
import { ENV } from '../config/constants.config';

@Module({
  imports: [
    CacheRedisModule.register(REDIS_CACHE, ENV.REDIS_CACHE_URL), // Redis 缓存模块,
  ],
  providers: [LocationsService],
  controllers: [LocationsController],
})
export class LocationsModule {}
