import 'dotenv/config';
import { defineConfig } from 'drizzle-kit';

export default defineConfig({
  dialect: 'postgresql',
  schema: './src/generated/drizzle', // 生成的 schema 文件位置
  out: './drizzle', // 迁移文件存放目录（暂不需要可忽略）
  dbCredentials: {
    url: process.env.DIRECT_URL!, // 使用你的数据库连接串
  },
  introspect: {
    casing: 'preserve', // 保持数据库中的原始命名，即 snake_case
  },
});
