import { Global, Module } from '@nestjs/common';
import { PrismaService } from './prisma.service';

@Global()
@Module({
  providers: [PrismaService],
  exports: [PrismaService],
})
export class PrismaModule {} // Prisma 模块
// 这个模块主要负责与数据库的交互，使用 Prisma ORM 来简化数据库操作。
