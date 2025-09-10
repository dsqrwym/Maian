import {
  ValidatorConstraint,
  ValidatorConstraintInterface,
  ValidationArguments,
} from 'class-validator';
import { Inject, Injectable } from '@nestjs/common';
import { Cache } from 'cache-manager';
import { MEMORY_CACHE } from '../../cache/memory/memory-cache';

@ValidatorConstraint({ name: 'IsBCP47Language', async: true })
@Injectable()
export class Bcp47LanguageValidator implements ValidatorConstraintInterface {
  constructor(@Inject(MEMORY_CACHE) private readonly cacheManager: Cache) {}

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
      await this.cacheManager.set(cacheKey, true, 604800); // 缓存7天
      return true;
    } catch {
      await this.cacheManager.set(cacheKey, false, 3600);
      return false;
    }
  }

  defaultMessage(args: ValidationArguments): string {
    return `${args.property} must be a valid BCP-47 language code`;
  }
}
