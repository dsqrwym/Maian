import { Injectable } from '@nestjs/common';
import { HashService } from '../../common/hash/hash.service';
import { ICreateAdminDto } from '../dto/create-admin.dto';
import { randomUUID } from 'node:crypto';
import { addDays } from '../../utils/date.utils';
import { AUTH_VERIFY_EMAIL_PATH } from '../../auth/auth.constants';
import { MailService } from 'src/mail/mail.service';
import { makeUsername } from '../../utils/user.utils';
import { DrizzleService } from 'src/drizzle/drizzle.service';
import {
  configurations,
  users,
  verification_tokens,
} from '../../generated/drizzle/schema';
import { eq } from 'drizzle-orm';
import { UserRole } from '../../generated/drizzle/enums';

@Injectable()
export class CreateAdminService {
  constructor(
    private readonly mailService: MailService,
    private readonly drizzleService: DrizzleService,
    private readonly hashService: HashService,
  ) {}

  async createAdmin(dto: ICreateAdminDto) {
    const { email, username } = dto;
    const result = await this.drizzleService.db.transaction(async (tx) => {
      const adminUsername = makeUsername(
        UserRole.ADMIN,
        username ?? randomUUID(),
      );
      const rows = await tx
        .insert(users)
        .values({
          email,
          username: adminUsername,
          password: 'NONE',
          role: 'ADMIN',
          status: 'PENDING_VERIFICATION',
        })
        .onConflictDoUpdate({
          target: users.email,
          setWhere: eq(users.status, 'PENDING_VERIFICATION'),
          set: { status: 'PENDING_VERIFICATION' }, // 随便更新点什么以触发返回
        })
        .returning({ id: users.id });

      const user = rows[0];

      // 另外查询语言设置（因为 INSERT 阶段 configurations 可能还没创建）
      const config = await tx.query.configurations.findFirst({
        where: eq(configurations.user_id, user.id),
        columns: { language: true },
      });

      const token = `${randomUUID()}`;
      const hashedToken = await this.hashService.hashWithCrypto(token);

      await tx.insert(verification_tokens).values({
        user_id: user.id,
        token: hashedToken,
        expires_at: addDays(new Date(), 7).toISOString(),
      });

      return {
        adminId: user.id,
        token,
        lang: config?.language ?? 'en',
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
