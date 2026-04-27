import {
  Injectable,
  OnModuleInit,
  BeforeApplicationShutdown,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { drizzle, NodePgDatabase } from 'drizzle-orm/node-postgres';
import { Pool } from 'pg';
import { PinoLogger } from 'nestjs-pino';
import { ENV } from '#/config/constants.config.js';
import * as schema from '../generated/drizzle/schema.js';
import * as relations from '../generated/drizzle/relations.js';

const FullSchema = { ...schema, ...relations };
export type DrizzleDb = NodePgDatabase<typeof FullSchema>;
@Injectable()
export class DrizzleService implements OnModuleInit, BeforeApplicationShutdown {
  public db: DrizzleDb; // 对外暴露的数据库操作对象
  private readonly pool: Pool;

  constructor(
    private readonly logger: PinoLogger,
    private readonly config: ConfigService,
  ) {
    this.logger.setContext(DrizzleService.name);
    const databaseUrl = this.config.get<string>(ENV.DATABASE_URL)!;
    const isDev = this.config.get<string>(ENV.NODE_ENV) !== 'production';

    // 创建 PostgreSQL 连接池
    this.pool = new Pool({
      connectionString: databaseUrl,
      // 可选：设置连接池大小、超时等
      max: 20,
      keepAlive: true, // 启用 TCP keep-alive，防止空闲连接被防火墙断开
      idleTimeoutMillis: 30000, // 空闲连接保留时间（毫秒）
      allowExitOnIdle: true, // 允许 Node.js 进程在无待处理请求时退出
    });
    // 创建 Drizzle 实例，并传入自定义 logger 以记录 SQL 查询
    this.db = drizzle(this.pool, {
      schema: FullSchema,
      // 根据环境变量决定是否开启日志
      ...(isDev && {
        logger: {
          logQuery: (query: string, params: unknown[]) => {
            this.logger.debug(
              {
                query: query.trim(),
                params,
              },
              'Drizzle Query',
            );
          },
        },
      }),
    });
  }

  async onModuleInit(): Promise<void> {
    // 测试连接是否成功
    try {
      const pool = await this.pool.connect(); // 尝试获取一个连接
      pool.release();
      this.logger.debug('Database connected successfully');
    } catch (error) {
      this.logger.error({ err: error }, 'Database connection failed');
      throw error; // 阻止应用启动
    }
  }

  async beforeApplicationShutdown(): Promise<void> {
    try {
      await this.pool.end();
      this.logger.debug('Database connection pool closed');
    } catch (error) {
      this.logger.error({ err: error }, 'Error closing database connection');
    }
  }
}
