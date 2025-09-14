import { CACHE_MANAGER, CacheModule } from '@nestjs/cache-manager';
import { Global, Module, Provider } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { PinoLogger } from 'nestjs-pino';
import { createKeyv } from '@keyv/redis';
import { IoRedisService } from './redis/ioredis.cache.service';

@Global()
@Module({
  providers: [IoRedisService],
  exports: [IoRedisService],
})
export class CacheRedisModule {
  /**
   * 注册一个独立的 Redis Cache 实例
   * @param name 唯一标识 token
   * @param urlEnvKey 用于读取对应的 Redis URL
   */
  static register(name: string, urlEnvKey: string) {
    const cacheModule = CacheModule.registerAsync({
      isGlobal: false,
      inject: [ConfigService, PinoLogger],
      useFactory: (configService: ConfigService, logger: PinoLogger) => {
        const redisUrl = configService.get<string>(urlEnvKey);

        if (!redisUrl) {
          throw new Error(`Missing Redis URL for ${urlEnvKey}`);
        }

        logger.info(
          { subsystem: name, event: 'init' },
          `Initializing ${name} Redis`,
        );

        return {
          stores: [createKeyv(redisUrl)],
        };
      },
    });

    const customProvider: Provider = {
      provide: name,
      useExisting: CACHE_MANAGER,
    };

    return {
      module: CacheRedisModule,
      imports: [cacheModule],
      providers: [customProvider],
      exports: [customProvider],
    };
  }
}
