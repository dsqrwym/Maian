import { Worker } from "worker_threads";
import { promisify } from "util";

export function runWorker(
    workerData: {
        type: 'hash' | 'compare',
        string: string,
        hasedString?: string,
        salt?: number
    }
): Promise<string | boolean> {
    return new Promise((resolve, reject) => {
        const worker = new Worker(__dirname + '../worker/bcrypt-worker.ts', {
            workerData,
        });
        const postMessageAsync = promisify(worker.postMessage).bind(worker);
        worker.on('message', (result) => {
            resolve(result);
        });
        worker.on('error', (error) => {
            reject(error);
        });
        worker.on('exit', (code) => {
            if (code !== 0) {
                reject(new Error(`Worker stopped with exit code ${code}`));
            }
        });

        postMessageAsync(workerData);
    });
}