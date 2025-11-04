import { Injectable } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';
import { CheckUserUsernameQueryDto } from '../dto/check-user-query.dto';
import { UserRole, UserStatus } from '@prisma/client';
import { makeUsername } from '../../utils/user.utils';

@Injectable()
export class CheckUserService {
  constructor(private readonly prismaService: PrismaService) {}
  async checkEmailUsed(email: string) {
    const user = await this.prismaService.users.findUnique({
      where: { email, NOT: { status: UserStatus.PENDING_VERIFICATION } },
      select: { id: true },
    });
    return !!user;
  }

  async checkUsernameUsed(query: CheckUserUsernameQueryDto) {
    console.log('checkUsername', query);
    let username = query.username;
    if (query.isAdmin) {
      username = makeUsername(UserRole.ADMIN, username);
    }
    if (query.wholesalerId) {
      username = makeUsername(query.wholesalerId, username);
    }
    const user = await this.prismaService.users.findUnique({
      where: { username },
      select: { id: true },
    });
    return !!user;
  }
}
