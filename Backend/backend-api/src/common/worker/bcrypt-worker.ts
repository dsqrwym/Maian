import { parentPort } from 'worker_threads';
import * as bcrypt from 'bcrypt';


// 哈希函数
async function hashString(string: string, salt: number): Promise<string> {
    return await bcrypt.hash(string, salt);
}

// 比较函数
async function compareString(string: string, hash: string): Promise<boolean> {
    return await bcrypt.compare(string, hash);
}

// 监听主线程消息 
parentPort?.on(
    'message',
    async (data: {
        type: 'hash' | 'compare',
        string: string,
        hasedString?: string,
        salt?: number
    }) => {
        let result: string | boolean;
        if (data.type === 'hash') {
            result = await hashString(data.string, data.salt || 10);
        } else if (data.type === 'compare') {
            result = await compareString(data.string, data.hasedString || '');
        } else {
            throw new Error('Invalid type');
        }
        // 将结果发送回主线程
        parentPort?.postMessage(result);
    })