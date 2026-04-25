import { Injectable } from '@nestjs/common';
import { Logger } from 'nestjs-pino';
import Redis from 'ioredis';
import { ConfigService } from '@nestjs/config';
import { ENV } from '@/config/constants.config';

/**
 * 我为什么要写 IoRedisService：
 * - 我需要一个全局复用的 ioredis 客户端（单例），统一连接、统一日志、统一错误处理。
 * - 许多第三方库（如 ThrottlerStorageRedisService）更适合接收“已经建立好的 Redis client”，
 *   这个服务提供 getClient() 以便直接复用同一条连接，避免到处 new Redis。
 *
 * 它解决了什么问题：
 * - 统一从 ENV.REDIS_CACHE_URL 建立连接，并集中监听 connect/error/close 等事件输出日志。
 * - 避免在不同模块各自 new Redis 导致连接数膨胀、调试分散、日志重复。
 * - 和 Nest DI 集成良好：哪里需要底层 Redis 操作，直接注入 IoRedisService，调用 getClient()。
 *
 * 我应该怎么用：
 * - 在需要传入 client 的库中复用：例如 new ThrottlerStorageRedisService(ioRedisService.getClient())
 * - 在需要执行原生命令的业务中使用：ioRedisService.getClient().set/get/pipeline/scan...
 * - 注意：这是“缓存/限流/会话”等通用 Redis 的共享客户端；Bull/BullMQ 等队列通常使用独立的 Redis（故障隔离）。
 *
 * 和 CacheRedisModule 的关系：
 * - CacheRedisModule（KeyvRedis）提供的是“缓存层”的高层抽象（cache.get/set）。
 * - IoRedisService 提供的是“原生 Redis 客户端”，适合需要客户端对象或复杂命令的场景。
 *
 * 给未来的我：
 * - 确保这个服务只初始化一次（模块标记为 @Global 并只在 AppModule 引入即可），避免多处实例化。
 * - 队列(BullMQ)若用独立 Redis（ENV.REDIS_BULL_URL），不要强行复用这个客户端，保持隔离。
 */
@Injectable()
export class IoRedisService {
  private readonly client: Redis;

  constructor(
    private readonly config: ConfigService,
    private readonly logger: Logger,
  ) {
    this.client = new Redis(
      this.config.get<string>(ENV.REDIS_CACHE_URL, 'redis://localhost:6379'),
    );
    this.client.on('connect', () => {
      this.logger.log('[IoRedisService] Redis connected');
    });
    this.client.on('error', (err) => {
      this.logger.error('[IoRedisService] Redis error:', err);
    });
    this.client.on('close', () => {
      this.logger.warn('[IoRedisService] Redis connection closed');
    });
  }

  getClient(): Redis {
    return this.client;
  }
}
