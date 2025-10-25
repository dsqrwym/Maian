import { Injectable } from '@nestjs/common';
import { Logger } from 'nestjs-pino';
import Redis from 'ioredis';
import { ConfigService } from '@nestjs/config';
import { ENV } from '../../config/constants.config';

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
