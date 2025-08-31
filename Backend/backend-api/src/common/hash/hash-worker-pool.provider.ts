import { ConfigService } from '@nestjs/config';
import { Provider } from '@nestjs/common';
import { Piscina } from 'piscina';
import * as path from 'path';
import * as os from 'os';
import { ENV } from '../../config/constants';

// 定义注入标识符，方便在Nest中引用
export const HASH_PROVIDE = 'HASH_WORKER_POOL';
// 默认最大线程数，通常是 CPU 核心数减1，保证系统不会被拖垮
export const WORKER_POOL_MAX_THREADS = Math.max(1, os.cpus().length - 1);
// 定义传递给worker线程的数据结构
export interface HashWorkerData {
  type: 'hash' | 'compare';
  algorithm: 'bcrypt' | 'crypto';
  string: string;
  hashedString?: string;
  salt?: number;
}

// NestJS Provider，注册Piscina线程池实例
export const HashWorkerPoolProvider: Provider = {
  provide: HASH_PROVIDE, // 注入名称
  inject: [ConfigService],
  useFactory: (config: ConfigService) => {
    return new Piscina({
      filename: path.resolve(__dirname, './hash-worker.js'),
      maxThreads:
        Number(config.get<number>(ENV.WORKER_POOL_MAX_THREADS)) ||
        WORKER_POOL_MAX_THREADS,
      idleTimeout:
        Number(config.get<number>(ENV.WORKER_POOL_IDLE_TIMEOUT)) || 60000,
      concurrentTasksPerWorker:
        Number(config.get<number>(ENV.WORKER_POOL_CONCURRENT_TASKS)) || 1,
    });
  },
};
