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
  }

  async onModuleInit() {
    await this.$connect();
    this.logger.debug('Prisma connected to database');
  }

  async beforeApplicationShutdown() {
    await this.$disconnect();
    this.logger.debug('Prisma disconnected from database');
  }
}
