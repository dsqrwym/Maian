import * as os from 'node:os';
import cluster from 'node:cluster';
import { bootstrap } from '#/bootstrap.js';

const numWorkers =
  Number(process.env.WORKERS) || Math.max(1, Math.floor(os.cpus().length / 2));
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
