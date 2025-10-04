import { Injectable } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';

@Injectable()
export class UserCheckService {
  constructor(private readonly prismaService: PrismaService) {}
  async checkMailUsed(email: string) {
    const user = await this.prismaService.users.findUnique({
      where: { email },
      select: { id: true },
    });
    return !!user;
  }
  async checkUsernameUsed(username: string) {
    const user = await this.prismaService.users.findUnique({
      where: { username },
      select: { id: true },
    });
    return !!user;
  }
}
