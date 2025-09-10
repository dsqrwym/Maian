import {
  Injectable,
  OnModuleInit,
  BeforeApplicationShutdown,
} from '@nestjs/common';
import { PrismaClient } from '../../prisma/generated/prisma';
import { PinoLogger } from 'nestjs-pino';

@Injectable()
export class PrismaService
  extends PrismaClient
  implements OnModuleInit, BeforeApplicationShutdown
{
  constructor(private readonly logger: PinoLogger) {
    super();
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
