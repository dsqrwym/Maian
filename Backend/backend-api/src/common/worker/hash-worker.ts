import * as bcrypt from 'bcrypt';
import * as crypto from 'crypto';
import { HashWorkerData } from '../provider/hash-worker-pool.provider'

// 哈希函数
async function hashWithBcrypt(string: string, salt: number): Promise<string> {
    return await bcrypt.hash(string, salt);
}

// 比较函数
async function compareWithBcrypt(string: string, hash: string): Promise<boolean> {
    return await bcrypt.compare(string, hash);
}

async function hashGeneral(string: string) {
    return crypto.createHash('sha256').update(string).digest('hex');
}

// 使用 crypto 哈希非密码数据（如文件内容）
function hashWithCrypto(input: string): string {
    return crypto.createHash('sha256').update(input).digest('hex');
}

// crypto 比较
function compareWithCrypto(input: string, hash: string): boolean {
    const hashed = hashWithCrypto(input);
    return hashed === hash;
}

export default async function (data: HashWorkerData): Promise<string | boolean> {
    if (data.type === 'hash') {
        return data.algorithm === 'bcrypt'
            ? await hashWithBcrypt(data.string, data.salt!)
            : hashWithCrypto(data.string);
    } else if (data.type === 'compare') {
        return data.algorithm === 'bcrypt'
            ? await compareWithBcrypt(data.string, data.hasedString!)
            : compareWithCrypto(data.string, data.hasedString!)
    } else {
        throw new Error('Unsupported operation');
    }
}