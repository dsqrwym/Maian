import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { runWorker } from '../helper/bcrypt-worker-helper';

@Injectable()
export class HashService {
    // 生成随机字符串
    constructor(private cinfig: ConfigService) { }

    async hashString(string: string): Promise<string> {
        const result = runWorker({ type: 'hash', string, salt: this.cinfig.get<number>('BCRYPT_SALT_ROUNDS') || 10 });
        return result as Promise<string>;
    }

    async compareString(string: string, hash: string): Promise<boolean> {
        const result = runWorker({ type: 'compare', string, hasedString: hash });
        return result as Promise<boolean>;
    }
}