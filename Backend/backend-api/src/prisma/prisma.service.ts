import { Injectable, OnModuleInit, BeforeApplicationShutdown } from '@nestjs/common';
import { PrismaClient } from '../../prisma/generated/prisma';


@Injectable()
export class PrismaService extends PrismaClient implements OnModuleInit, BeforeApplicationShutdown {
    constructor() {
        super();
    }

    async onModuleInit() {
        await this.$connect();
        console.log('Prisma connected to database');
    }

    async beforeApplicationShutdown() {
        await this.$disconnect();
        console.log('Prisma disconnected from database');
    }
}