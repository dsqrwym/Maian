import { CACHE_MANAGER, CacheModule } from '@nestjs/cache-manager';
import { Global, Module, Provider } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { PinoLogger } from 'nestjs-pino';
import KeyvRedis from '@keyv/redis';
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
   *
   * 我为什么要写 CacheRedisModule.register：
   * - 我需要“按用途”注册一个独立的 Redis 缓存实例，并且要无缝对接 Nest 的 CacheModule。
   * - 我用 KeyvRedis 作为 Cache store，这样就能用 Nest 的 Cache 接口（get/set）做业务缓存，
   *   同时保留 Redis 的性能和可观测性（在这里集中打日志和监听错误）。
   *
   * 它解决了什么问题：
   * - 统一在一个地方用 urlEnvKey 读取 Redis 连接串，并集中监听 connect/error/reconnecting/end。
   * - 通过 name（token）把这个缓存实例暴露出去，其他模块只需要 @Inject(name) 就能拿到 Cache。
   * - 需要多个“职责分离”的缓存实例时，可以多次调用 register，但每个实例必须使用不同的 token。
   *
   * 我应该怎么用：
   * - 在 AppModule 里注册一次全局可用的缓存（推荐单实例）：CacheRedisModule.register(REDIS_CACHE, ENV.REDIS_CACHE_URL)
   * - 其他功能模块不要重复注册相同 token（否则会重复初始化、产生多条连接和重复日志）。
   * - 使用时：构造函数里 @Inject(REDIS_CACHE) private readonly cache: Cache 即可直接用 cache.get/set。
   *
   * 什么时候需要多个实例：
   * - 确实需要“职责隔离/不同重试策略/不同数据保留策略”的缓存时，才用不同 token 再注册一个实例，
   *   例如：REDIS_CACHE_DEFAULT / REDIS_CACHE_HEAVY_TASKS（注意不要复用同一个 token）。
   *
   * 和 IoRedisService 的关系：
   * - 这是“缓存层”的抽象，偏业务读写（Cache 接口）。
   * - IoRedisService 是“底层 Redis 客户端”，当我需要把“现成的 ioredis client”传给第三方库（比如限流）
   *   或者需要执行原生命令（pipeline/scan，AUTH Token 里有使用）时，用 IoRedisService。
   *
   * 给未来的我：
   * - 不要在多个模块重复调用 register 同一个 token，会导致多次初始化与连接。
   * - 如果要做键空间隔离，优先用“key 前缀”或“不同 DB index”；若还不够，再考虑多实例。
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
          `[Cache Module] Initializing ${name} Redis`,
        );

        const store = new KeyvRedis(redisUrl);

        store.client.on('error', () => {
          logger.error(
            { subsystem: name, event: 'error' },
            `[Cache Module] Error connecting to ${name} Redis`,
          );
        });

        store.client.on('connect', () => {
          logger.info(
            { subsystem: name, event: 'connect' },
            `[Cache Module] Connected to ${name} Redis`,
          );
        });

        store.client.on('reconnecting', () => {
          logger.warn(
            { subsystem: name, event: 'reconnecting' },
            `[Cache Module] Reconnecting to ${name} Redis`,
          );
        });

        store.client.on('end', () => {
          logger.warn(
            { subsystem: name, event: 'end' },
            `[Cache Module] Disconnected from ${name} Redis`,
          );
        });

        return {
          stores: [store],
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
