import { Injectable } from '@nestjs/common';
import { PrismaService } from 'src/prisma/prisma.service';
import { UserRole, UserStatus } from 'src/generated/prisma/client';
import { HashService } from '../../common/hash/hash.service';
import { CreateAdminDto } from '../dto/create-admin.dto';
import { randomUUID } from 'node:crypto';
import { addDays } from '../../utils/date.utils';
import { AUTH_VERIFY_EMAIL_PATH } from '../../auth/auth.constants';
import { MailService } from 'src/mail/mail.service';
import { makeUsername } from '../../utils/user.utils';

@Injectable()
export class CreateAdminService {
  constructor(
    private readonly mailService: MailService,
    private readonly prismaService: PrismaService,
    private readonly hashService: HashService,
  ) {}

  async createAdmin(dto: CreateAdminDto) {
    const { email, username } = dto;
    const result = await this.prismaService.$transaction(async (tx) => {
      const adminUsername = makeUsername(
        UserRole.ADMIN,
        username ?? randomUUID(),
      );
      const user = await this.prismaService.users.upsert({
        where: { email, username, status: UserStatus.PENDING_VERIFICATION },
        create: {
          email,
          username: adminUsername,
          password: 'NONE',
          role: UserRole.ADMIN,
          status: UserStatus.PENDING_VERIFICATION,
        },
        update: {},
        select: { id: true, configurations: { select: { language: true } } },
      });

      const token = `${randomUUID()}`;
      const hashedToken = await this.hashService.hashWithCrypto(token);

      await tx.verification_tokens.create({
        data: {
          user_id: user.id,
          token: hashedToken,
          expires_at: addDays(new Date(), 7),
        },
      });

      return {
        adminId: user.id,
        token,
        lang: user.configurations?.language ?? 'en',
      };
    });

    const link = `${AUTH_VERIFY_EMAIL_PATH}?token=${result.token}&userId=${result.adminId}&lang=${result.lang}`;

    await this.mailService.sendAdminVerifyEmail({
      lang: result.lang,
      link: link,
      to: dto.email,
    });
  }
}
