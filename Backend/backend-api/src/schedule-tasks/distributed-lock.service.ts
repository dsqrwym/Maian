import { Injectable } from '@nestjs/common';
import { randomUUID } from 'node:crypto';
import { PinoLogger } from 'nestjs-pino';
import { IoRedisService } from '../cache/redis/ioredis.cache.service';

@Injectable()
export class DistributedLockService {
  constructor(
    private readonly ioRedisService: IoRedisService,
    private readonly logger: PinoLogger,
  ) {
    this.logger.setContext(DistributedLockService.name);
  }

  async acquire(lockKey: string, ttlMs: number): Promise<string | null> {
    const lockValue = randomUUID();
    const result = await this.ioRedisService
      .getClient()
      .set(lockKey, lockValue, 'PX', ttlMs, 'NX');

    return result === 'OK' ? lockValue : null;
  }

  async release(lockKey: string, lockValue: string): Promise<void> {
    const releaseScript = `
      if redis.call("get", KEYS[1]) == ARGV[1] then
        return redis.call("del", KEYS[1])
      end
      return 0
    `;

    await this.ioRedisService
      .getClient()
      .eval(releaseScript, 1, lockKey, lockValue);
  }

  async runWithLock<T>(
    lockKey: string,
    ttlMs: number,
    task: () => Promise<T>,
  ): Promise<T | null> {
    const lockValue = await this.acquire(lockKey, ttlMs);

    if (!lockValue) {
      this.logger.debug({ lockKey }, 'Skip scheduled task because lock is held');
      return null;
    }

    try {
      return await task();
    } finally {
      try {
        await this.release(lockKey, lockValue);
      } catch (err) {
        this.logger.error(
          { err, lockKey },
          'Failed to release distributed lock',
        );
      }
    }
  }
}
