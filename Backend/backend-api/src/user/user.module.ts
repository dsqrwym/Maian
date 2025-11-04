import { Module } from '@nestjs/common';
import { CheckUserService } from './services/check-user.service';
import { CacheRedisModule } from '../cache/cache.redis.module';
import { REDIS_CACHE } from '../cache/redis/cache.redis.token';
import { ENV } from '../config/constants.config';
import { CheckUserController } from './controllers/check-user.controller';
import { FindUserController } from './controllers/find-user.controller';
import { FindUserService } from './services/find-user.service';
import { RouterModule } from '@nestjs/core';

@Module({
  imports: [
    RouterModule.register([
      {
        path: 'user',
        module: UserModule,
      },
    ]),
    CacheRedisModule.register(REDIS_CACHE, ENV.REDIS_CACHE_URL),
  ],
  controllers: [CheckUserController, FindUserController],
  providers: [CheckUserService, FindUserService],
})
export class UserModule {}
