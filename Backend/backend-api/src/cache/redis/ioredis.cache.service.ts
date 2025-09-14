import { Injectable } from '@nestjs/common';
import Redis from 'ioredis';
import { ConfigService } from '@nestjs/config';
import { ENV } from '../../config/constants.config';

@Injectable()
export class IoRedisService {
  private readonly client: Redis;

  constructor(private config: ConfigService) {
    this.client = new Redis(
      this.config.get<string>(ENV.REDIS_CACHE_URL, 'redis://localhost:6379'),
    );
  }

  getClient(): Redis {
    return this.client;
  }
}
