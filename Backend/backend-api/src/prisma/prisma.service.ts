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

    super({
      adapter,
      log: ['query', 'info', 'warn', 'error'],
    });
    this.logger.setContext(PrismaService.name);
  }

  async onModuleInit(): Promise<void> {
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
}
