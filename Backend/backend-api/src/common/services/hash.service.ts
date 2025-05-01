import { Inject, Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import Piscina from 'piscina';

@Injectable()
export class HashService {
    // 生成随机字符串
    constructor(@Inject('HASH_WORKER_POOL') private readonly pool: Piscina, private config: ConfigService) { }

    async hashWithBcrypt(string: string): Promise<string> {
        const result = this.pool.run({ type: 'hash', algorithm: 'bcrypt', string, salt: Number(this.config.get<number>('BCRYPT_SALT_ROUNDS')) || 10 });
        return result as Promise<string>;
    }

    async compareWithBcrypt(string: string, hash: string): Promise<boolean> {
        const result = this.pool.run({ type: 'compare', algorithm: 'bcrypt', string, hasedString: hash });
        return result as Promise<boolean>;
    }

    async hashWithCrypto(input: string): Promise<string> {
        const result = this.pool.run({ type: 'hash', algorithm: 'crypto', string: input });
        return result as Promise<string>;
    }

    async compareCrypto(input: string, hash: string): Promise<boolean> {
        const result = this.pool.run({ type: 'compare', algorithm: 'crypto', string: input, hasedString: hash });
        return result as Promise<boolean>;
    }
}