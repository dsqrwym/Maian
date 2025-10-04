import {
  ValidatorConstraint,
  ValidatorConstraintInterface,
  ValidationArguments,
} from 'class-validator';
import { Inject, Injectable } from '@nestjs/common';
import type { Cache } from 'cache-manager';
import { REDIS_CACHE } from '../../cache/redis/cache.redis.token';
import { DAY, HOUR } from '../../utils/date.utils';

@ValidatorConstraint({ name: 'IsBCP47Language', async: true })
@Injectable()
export class Bcp47LanguageValidator implements ValidatorConstraintInterface {
  constructor(@Inject(REDIS_CACHE) private readonly cacheManager: Cache) {}

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  async validate(value: string, _args: ValidationArguments): Promise<boolean> {
    const cacheKey = `validation:bcp47:${value}`;

    const cachedResult = await this.cacheManager.get<boolean | undefined>(
      cacheKey,
    );
    if (cachedResult !== undefined && cachedResult !== null) {
      return cachedResult;
    }

    try {
      new Intl.DateTimeFormat(value);
      await this.cacheManager.set(cacheKey, true, 7 * DAY); // 缓存7天
      return true;
    } catch {
      await this.cacheManager.set(cacheKey, false, HOUR);
      return false;
    }
  }

  defaultMessage(args: ValidationArguments): string {
    return `${args.property} must be a valid BCP-47 language code`;
  }
}
