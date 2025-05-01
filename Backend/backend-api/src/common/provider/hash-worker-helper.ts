import path from 'path';
import Piscina from 'piscina';
import * as os from 'os';

export class HashWorkerData {
    type: 'hash' | 'compare';
    algorithm: 'bcrypt' | 'crypto';
    string: string;
    hasedString?: string;
    salt?: number;
}

// Piscina是一个轻量级的线程池库，用于在Node.js中创建和管理工作线程
const MaxThreads = Math.max(1, os.cpus().length - 1); // 限制最大CPU核心数

const pool = new Piscina({
    filename: path.resolve(__dirname, '../worker/hash-worker.js'),
    maxThreads: MaxThreads,
    idleTimeout: 60_000, //空闲超时释放
    concurrentTasksPerWorker: 2, //每个worker并发任务数
});

export function runWorker(workerData: HashWorkerData): Promise<string | boolean> {
    return pool.run(workerData) as Promise<string | boolean>;
}