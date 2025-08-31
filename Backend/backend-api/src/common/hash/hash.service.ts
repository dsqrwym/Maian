import { Inject, Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import Piscina from 'piscina';
import { HASH_PROVIDE } from './hash-worker-pool.provider';
import { ENV } from '../../config/constants';

@Injectable()
export class HashService {
  // 构造函数注入Piscina线程池实例和配置服务
  constructor(
    @Inject(HASH_PROVIDE) private readonly pool: Piscina,
    private config: ConfigService,
  ) {}

  async hashWithBcrypt(string: string): Promise<string> {
    const result = this.pool.run({
      type: 'hash',
      algorithm: 'bcrypt',
      string,
      salt: Number(this.config.get<number>(ENV.BCRYPT_SALT_ROUNDS)) || 10,
    });
    return result as Promise<string>;
  }

  async compareWithBcrypt(string: string, hash: string): Promise<boolean> {
    const result = this.pool.run({
      type: 'compare',
      algorithm: 'bcrypt',
      string,
      hashedString: hash,
    });
    return result as Promise<boolean>;
  }

  async hashWithCrypto(input: string): Promise<string> {
    const result = this.pool.run({
      type: 'hash',
      algorithm: 'crypto',
      string: input,
    });
    return result as Promise<string>;
  }

  async compareCrypto(input: string, hash: string): Promise<boolean> {
    const result = this.pool.run({
      type: 'compare',
      algorithm: 'crypto',
      string: input,
      hashedString: hash,
    });
    return result as Promise<boolean>;
  }
}
