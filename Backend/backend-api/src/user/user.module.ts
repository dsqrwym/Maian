import { Module } from '@nestjs/common';
import { CheckUserService } from './services/check-user.service';
import { CacheRedisModule } from '../cache/cache.redis.module';
import { REDIS_CACHE } from '../cache/redis/cache.redis.token';
import { ENV } from '../config/constants.config';
import { CheckUserController } from './controllers/check-user.controller';

@Module({
  imports: [CacheRedisModule.register(REDIS_CACHE, ENV.REDIS_CACHE_URL)],
  controllers: [CheckUserController],
  providers: [CheckUserService],
})
export class UserModule {}
