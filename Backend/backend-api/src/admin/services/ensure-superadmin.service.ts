import { Injectable } from '@nestjs/common';
import { Logger } from 'nestjs-pino';
import { ENV } from '#/config/constants.config.js';
import { ConfigService } from '@nestjs/config';
import { UserRole } from '#/generated/drizzle/enums.js';
import { HashService } from '#/common/hash/hash.service.js';
import { makeUsername } from '#/utils/user.utils.js';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import { users } from '#/generated/drizzle/schema.js';
import { eq } from 'drizzle-orm';

@Injectable()
export class EnsureSuperAdminService {
  constructor(
    private readonly logger: Logger,
    private readonly configService: ConfigService,
    private readonly drizzleService: DrizzleService,
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

    const admin = await this.drizzleService.db.query.users.findFirst({
      columns: { username: true, email: true },
      where: eq(users.role, 'SUPERADMIN'),
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

    await this.drizzleService.db
      .insert(users)
      .values({
        email: superAdminMail,
        username: superAdminUsername,
        password: hashedPassword,
        role: 'SUPERADMIN',
        status: 'APPROVED',
      })
      .onConflictDoUpdate({
        target: [users.email, users.username],
        set: {
          email: superAdminMail,
          username: superAdminUsername,
          password: hashedPassword,
          role: 'SUPERADMIN',
          status: 'APPROVED',
        },
      });

    this.logger.log('Super Admin created', superAdminMail, superAdminUsername);
  }
}
