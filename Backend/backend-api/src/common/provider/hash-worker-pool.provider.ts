import { ConfigService } from '@nestjs/config';
import { Provider } from '@nestjs/common';
import { Piscina } from 'piscina';
import * as path from 'path';
import * as os from 'os';

const WORKER_POOL_MAX_THREADS = os.cpus().length - 1;

export interface HashWorkerData {
  type: 'hash' | 'compare';
  algorithm: 'bcrypt' | 'crypto';
  string: string;
  hasedString?: string;
  salt?: number;
}

export const HashWorkerPoolProvider: Provider = {
  provide: 'HASH_WORKER_POOL',
  inject: [ConfigService],
  useFactory: (config: ConfigService) => {
    return new Piscina({
      filename: path.resolve(__dirname, '../worker/hash-worker.js'),
      maxThreads: config.get<number>('WORKER_POOL_MAX_THREADS') || WORKER_POOL_MAX_THREADS,
      idleTimeout: Number(config.get<number>('WORKER_POOL_IDLE_TIMEOUT')) || 60000,
      concurrentTasksPerWorker: Number(config.get<number>('WORKER_POOL_CONCURRENT_TASKS')) || 1,
    });
  },
};
