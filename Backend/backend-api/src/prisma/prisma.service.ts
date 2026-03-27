import {
  Injectable,
  OnModuleInit,
  BeforeApplicationShutdown,
} from '@nestjs/common';
import { PrismaClient } from 'src/generated/prisma/client';
import { PinoLogger } from 'nestjs-pino';
import { PrismaPg } from '@prisma/adapter-pg';
import { ConfigService } from '@nestjs/config';
import { ENV } from '../config/constants.config';

@Injectable()
export class PrismaService
  extends PrismaClient
  implements OnModuleInit, BeforeApplicationShutdown
{
  constructor(
    private readonly logger: PinoLogger,
    readonly config: ConfigService,
  ) {
    const adapter = new PrismaPg({
      connectionString: config.get<string>(ENV.DATABASE_URL),
    });
    const isDev =
      config.get<string>(ENV.NODE_ENV, 'development') !== 'production';
    super({
      adapter,
      ...(isDev
        ? {
            log: [
              { emit: 'event', level: 'query' },
              { emit: 'event', level: 'info' },
              { emit: 'event', level: 'warn' },
              { emit: 'event', level: 'error' },
            ] as const,
          }
        : {}),
    });
    this.logger.setContext(PrismaService.name);
  }

  async onModuleInit(): Promise<void> {
    this.bindLogsToPino();
    try {
      await this.$connect();
      this.logger.debug('Connected to database');
    } catch (error: unknown) {
      this.logger.error({ err: error }, 'Database connection failed');
      throw error;
    }
  }

  async beforeApplicationShutdown(): Promise<void> {
    try {
      await this.$disconnect();
      this.logger.debug('Disconnected from database');
    } catch (error: unknown) {
      this.logger.error({ err: error }, 'Error while disconnecting database');
    }
  }

  private bindLogsToPino() {
    // @ts-expect-error - Prisma 内部类型有时较难匹配，这里直接订阅
    this.$on('query', (e: any) => {
      this.logger.debug(
        {
          // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
          query: JSON.stringify(e.query),
          // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment,@typescript-eslint/no-unsafe-member-access
          params: e.params,
          // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
          duration: `${e.duration}ms`,
        },
        'Prisma Query',
      );
    });

    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    this.$on('info', (e: any) => this.logger.info(e.message));

    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    this.$on('warn', (e: any) => this.logger.warn(e.message));

    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    this.$on('error', (e: any) => this.logger.error(e.message));
  }
}
