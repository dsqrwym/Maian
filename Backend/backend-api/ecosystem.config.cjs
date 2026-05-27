const os = require('node:os');
const cpuCount = Math.max(os.cpus().length - 1, 1);
const instances =
  process.env.PM2_INSTANCES === 'max'
    ? 'max'
    : Number(process.env.PM2_INSTANCES || cpuCount);

module.exports = {
  apps: [
    {
      name: 'maian-backend',
      script: './dist/src/main.js',
      exec_mode: 'cluster', // 多进程模式
      instances: instances, // 根据CPU核心数最大化进程
      watch: false,
      error_file: './logs/pm2-error.log',
      out_file: './logs/pm2-out.log',
      // Pino 已经在内部生成了高性能的 JSON 格式时间戳。
      // 如果 PM2 再往行首追加纯文本时间，会破坏整行的 JSON 结构，导致后续解析失败。
      time: false,
      env: {
        NODE_ENV: 'development',
      },
      env_production: {
        NODE_ENV: 'production',
      },
    },
  ],
};
