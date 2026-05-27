/**
 * 全局类型扩展 (Global Type Augmentation)
 * 必须使用 declare global，让 TypeScript 编译器知道 BigInt 实例上新增了 toJSON 方法。
 * 这样在后续代码中调用 JSON.stringify 时，TS 不会报错。
 */
declare global {
  interface BigInt {
    toJSON(): string;
  }
}

/**
 * 运行时原型注入 (Runtime Polyfill)
 * JSON.stringify 在处理对象时，如果发现对象有 toJSON 方法，会调用它。
 * 因为原生 BigInt 没有这个方法，导致序列化报错。这里补全它。
 */
BigInt.prototype.toJSON = function (this: bigint): string {
  return this.toString();
};
/**
 * 理论上以上操作应该在程序运行之前，所以程序入口为下
 */

import * as os from 'node:os';
import cluster from 'node:cluster';
import { bootstrap } from '#/bootstrap.js';

const numWorkers =
  Number(process.env.CLUSTER_INSTANCES) || Math.max(1, os.cpus().length - 1);
const mainName = 'Cluster Main';
if (cluster.isPrimary) {
  console.log(
    `[${mainName}]: Cluster main started, PID ${process.pid} is running`,
  );
  console.log(`[${mainName}]: Forking ${numWorkers} workers...`);

  for (let i = 0; i < numWorkers; i++) {
    cluster.fork();
  }

  cluster.on('exit', (worker, code, signal) => {
    console.log(
      `[${mainName}]: Worker ${worker.process.pid} died (code: ${code}, signal: ${signal}). Restarting...`,
    );
    cluster.fork();
  });

  cluster.on('online', (worker) => {
    console.log(`[${mainName}]: Worker ${worker.process.pid} is online`);
  });
} else {
  const workerName = `Cluster Worker ${process.pid}`;
  bootstrap()
    .then(() => {
      console.log(
        `[${workerName}]: Worker started, PID ${process.pid} is running`,
      );
    })
    .catch((error) => {
      console.error(
        `[${mainName}]: Bootstrap failed: ${JSON.stringify(error)}`,
      );
      process.exit(1);
    });
}
