import {
  ValidatorConstraint,
  ValidatorConstraintInterface,
  ValidationArguments,
} from 'class-validator';
import { Inject, Injectable } from '@nestjs/common';
import type { Cache } from 'cache-manager';
import { REDIS_CACHE } from '#/cache/redis/cache.redis.token.js';
import { DAY, HOUR } from '#/utils/date.utils.js';

@ValidatorConstraint({ name: 'IsIANA', async: true }) // 让Nest可以管理这个类
@Injectable()
export class IanaTimezoneValidator implements ValidatorConstraintInterface {
  constructor(@Inject(REDIS_CACHE) private readonly cacheManager: Cache) {}

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  async validate(value: string, _args: ValidationArguments): Promise<boolean> {
    const cacheKey = `validation:iana_timezone_${value}`;

    const cachedResult = await this.cacheManager.get<boolean | undefined>(
      cacheKey,
    );
    if (cachedResult !== undefined && cachedResult !== null) {
      return cachedResult;
    }

    try {
      new Intl.DateTimeFormat('en-US', { timeZone: value });
      await this.cacheManager.set(cacheKey, true, 7 * DAY); // 缓存7天
      return true;
    } catch {
      await this.cacheManager.set(cacheKey, false, HOUR);
      return false;
    }
  }

  defaultMessage(args: ValidationArguments): string {
    return `${args.property} must be a valid IANA timezone`;
  }
}
