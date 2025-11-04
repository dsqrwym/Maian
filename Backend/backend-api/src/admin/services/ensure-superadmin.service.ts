import { Injectable } from '@nestjs/common';
import { Logger } from 'nestjs-pino';
import { ENV } from '../../config/constants.config';
import { ConfigService } from '@nestjs/config';
import { PrismaService } from 'src/prisma/prisma.service';
import { UserRole, UserStatus } from '@prisma/client';
import { HashService } from '../../common/hash/hash.service';
import { makeUsername } from '../../utils/user.utils';

@Injectable()
export class EnsureSuperAdminService {
  constructor(
    private readonly logger: Logger,
    private readonly configService: ConfigService,
    private readonly prismaService: PrismaService,
    private readonly hashService: HashService,
  ) {}

  async ensureSuperAdmin() {
    this.logger.log('Checking for Super Admin...');

    const superAdminMail = this.configService.get<string>(
      ENV.SUPERADMIN_EMAIL,
      'superadmin@dsqrwym.com',
    );
    const superAdminUsername = makeUsername(
      UserRole.ADMIN,
      this.configService.get<string>(ENV.SUPERADMIN_USERNAME, 'superadmin'),
    );

    const superAdminPassword = this.configService.get<string>(
      ENV.SUPERADMIN_PASSWORD,
      'superadmin',
    );

    const admin = await this.prismaService.users.findFirst({
      where: { role: UserRole.SUPERADMIN },
      select: { username: true, email: true },
    });

    if (admin) {
      this.logger.log(
        'Super Admin already exists',
        admin.username,
        admin.email,
      );
      return;
    }

    const hashedPassword =
      await this.hashService.hashWithBcrypt(superAdminPassword);

    await this.prismaService.users.upsert({
      where: { email: superAdminMail, username: superAdminUsername },
      create: {
        email: superAdminMail,
        username: superAdminUsername,
        password: hashedPassword,
        role: UserRole.SUPERADMIN,
        status: UserStatus.APPROVED,
      },
      update: {
        email: superAdminMail,
        username: superAdminUsername,
        password: hashedPassword,
        role: UserRole.SUPERADMIN,
        status: UserStatus.APPROVED,
      },
    });

    this.logger.log('Super Admin created', superAdminMail, superAdminUsername);
  }
}
