import { Module } from '@nestjs/common';
import { UserCheckService } from './services/user-check.service';
import { CacheRedisModule } from '../cache/cache.redis.module';
import { REDIS_CACHE } from '../cache/redis/cache.redis.token';
import { ENV } from '../config/constants.config';
import { UserChekController } from './controllers/user-chek.controller';

@Module({
  imports: [CacheRedisModule.register(REDIS_CACHE, ENV.REDIS_CACHE_URL)],
  controllers: [UserChekController],
  providers: [UserCheckService],
})
export class UserModule {}
