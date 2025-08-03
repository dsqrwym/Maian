import {
  ValidatorConstraint,
  ValidatorConstraintInterface,
  ValidationArguments,
} from 'class-validator';
import { Inject, Injectable } from '@nestjs/common';
import { Cache } from 'cache-manager';
import { CACHE_MANAGER } from '@nestjs/cache-manager';

@ValidatorConstraint({ name: 'IsIANA', async: true }) // 让Nest可以管理这个类
@Injectable()
export class IanaTimezoneValidator implements ValidatorConstraintInterface {
  constructor(@Inject(CACHE_MANAGER) private readonly cacheManager: Cache) {}

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
      await this.cacheManager.set(cacheKey, true, 604800); // 缓存7天
      return true;
    } catch {
      await this.cacheManager.set(cacheKey, false, 3600);
      return false;
    }
  }

  defaultMessage(args: ValidationArguments): string {
    return `${args.property} must be a valid IANA timezone`;
  }
}
