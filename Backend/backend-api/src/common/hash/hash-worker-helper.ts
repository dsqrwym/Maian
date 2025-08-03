import path from 'path';
import Piscina from 'piscina';
import {
  HashWorkerData,
  WORKER_POOL_MAX_THREADS,
} from './hash-worker-pool.provider';

// 创建 Piscina 线程池实例，指定 worker 文件位置和线程数配置
const pool = new Piscina({
  filename: path.resolve(__dirname, '../worker/hash-worker.js'),
  maxThreads: WORKER_POOL_MAX_THREADS,
  idleTimeout: 60_000, //空闲超时释放
  concurrentTasksPerWorker: 2, //每个worker并发任务数
});

// 导出一个函数，向线程池提交任务，返回Promise
export function runWorker(
  workerData: HashWorkerData,
): Promise<string | boolean> {
  return pool.run(workerData) as Promise<string | boolean>;
}
