import {
  Injectable,
  NotFoundException,
  UnauthorizedException,
} from '@nestjs/common';
import { Logger } from 'nestjs-pino';
import { UserRole, UserStatus } from 'src/generated/drizzle/enums';
import {
  ISendVerificationCodeDto,
  IVerifyCodeDto,
  IVerifyEmailQueryDto,
  VerifyCodeResponseDto,
} from '../dto/verification.dto';
import { maskEmail } from '../../utils/email.utils';
import { addMinutes } from '../../utils/date.utils';
import { AUTH_ERROR, VerificationEmailType } from '../auth.constants';
import { TooManyRequestsExceptions } from '../../common/exceptions/too-many-requests.exceptions';
import {
  generateUniformRandomDigits,
  generateUniformStrongPassword,
} from '../../utils/random.utils';
import { MailService } from '../../mail/mail.service';
import { HashService } from 'src/common/hash/hash.service';
import { randomUUID } from 'node:crypto';
import { RegisterEmailJob } from '../../mail/mail.types';
import { WholesalerProfileType } from '../../enterprise/types/wholesaler-profile.type';
import { renderTemplate } from '../../utils/hbs-renderer';
import { FastifyReply } from 'fastify';
import { I18nService } from 'nestjs-i18n';
import { DrizzleDb, DrizzleService } from '../../drizzle/drizzle.service';
import { and, desc, eq, gt, sql } from 'drizzle-orm';
import {
  configurations,
  users,
  verification_tokens,
} from '../../generated/drizzle/schema';

@Injectable()
export class VerificationService {
  constructor(
    private readonly i18nService: I18nService,
    //private readonly prismaService: PrismaService,
    private readonly drizzleService: DrizzleService,
    private readonly mailService: MailService,
    private readonly hashService: HashService,
    private readonly logger: Logger,
  ) {}

  async sendVerificationCode(
    sendVerificationDto: ISendVerificationCodeDto,
    emailType: VerificationEmailType,
  ) {
    const email = sendVerificationDto.email;
    const markedEmail = maskEmail(email);
    this.logger.debug({ email: markedEmail }, '[sendVerificationCode] start');
    const user = await this.drizzleService.db.query.users.findFirst({
      where: eq(users.email, email),
      columns: { id: true, username: true },
      with: {
        configurations: { columns: { language: true } },
        verification_tokens: {
          where: gt(
            verification_tokens.created_at,
            addMinutes(new Date(), -1).toISOString(),
          ),
          limit: 1,
          columns: { id: true },
        },
      },
    });

    if (!user) {
      this.logger.warn(
        { email: markedEmail },
        '[sendVerificationCode] email not exist',
      );
      throw new NotFoundException(AUTH_ERROR.USER_NOT_FOUND);
    }

    // 防止恶意重复刷验证码
    if (user.verification_tokens.length > 0) {
      this.logger.warn(
        { userId: user.id, email: markedEmail },
        '[sendVerificationCode] rate limited',
      );
      throw new TooManyRequestsExceptions(
        AUTH_ERROR.VERIFICATION_CODE_RATE_LIMIT,
      );
    }

    const code = generateUniformRandomDigits(6);

    const hashedCode = await this.hashService.hashWithCrypto(code);

    await this.drizzleService.db.insert(verification_tokens).values({
      user_id: user.id,
      token: hashedCode,
      expires_at: addMinutes(new Date(), 10).toISOString(), // 10 分钟过期
    });

    switch (emailType) {
      case VerificationEmailType.NORMAL_REGISTER: {
        const registerEmailJob: RegisterEmailJob = {
          to: email,
          lang: user.configurations[0]?.language,
          link: sendVerificationDto.deepLink ?? '',
          code: code,
        };
        await this.mailService.sendNormalRegisterEmail(registerEmailJob);
        break;
      }
      case VerificationEmailType.RESET_PASSWORD:
        await this.mailService.sendResetPassword({
          to: email,
          name: user.username ?? email,
          lang: user.configurations[0]?.language,
          code: code,
        });
        break;
    }

    this.logger.debug(
      { userId: user.id, email: markedEmail },
      `[sendVerificationCode] sent , language${user.configurations[0]?.language}`,
    );
  }

  async verifyCode(verifyCode: IVerifyCodeDto, expiresMinutes: number = 10) {
    const userCode = await this.drizzleService.db.query.users.findFirst({
      where: eq(users.email, verifyCode.email),
      columns: {},
      with: {
        verification_tokens: {
          where: and(
            eq(verification_tokens.is_used, false),
            gt(verification_tokens.expires_at, new Date().toISOString()),
          ),
          columns: { token: true, id: true, attempts: true, expires_at: true },
          orderBy: desc(verification_tokens.created_at),
          limit: 1,
        },
      },
    });

    if (!userCode || userCode?.verification_tokens?.length === 0) {
      this.logger.warn(
        { email: maskEmail(verifyCode.email) },
        '[verifyCode] code not found or expired',
      );
      throw new NotFoundException(AUTH_ERROR.VERIFICATION_CODE_NOT_FOUND);
    }

    const isValidCode = await this.hashService.compareCrypto(
      verifyCode.code,
      userCode.verification_tokens[0].token,
    );

    if (!isValidCode) {
      const [updatedToken] = await this.drizzleService.db
        .update(verification_tokens)
        .set({ attempts: sql`${verification_tokens.attempts} + 1` })
        .where(eq(verification_tokens.id, userCode.verification_tokens[0].id))
        .returning({ attempts: verification_tokens.attempts });

      if (updatedToken.attempts >= 3) {
        await this.drizzleService.db
          .update(verification_tokens)
          .set({ is_used: true })
          .where(
            eq(verification_tokens.id, userCode.verification_tokens[0].id),
          );
        this.logger.warn(
          { email: maskEmail(verifyCode.email) },
          '[verifyCode] too many attempts',
        );
        throw new TooManyRequestsExceptions(
          AUTH_ERROR.VERIFICATION_CODE_TOO_MANY_ATTEMPTS,
        );
      }
      this.logger.warn(
        { email: maskEmail(verifyCode.email), attempts: updatedToken.attempts },
        '[verifyCode] incorrect code',
      );
      throw new UnauthorizedException(AUTH_ERROR.VERIFICATION_CODE_INCORRECT);
    }

    const response: VerifyCodeResponseDto = {
      verification_id: userCode.verification_tokens[0].id,
      token: randomUUID(),
      expires_at: addMinutes(new Date(), expiresMinutes),
    };

    await this.drizzleService.db
      .update(verification_tokens)
      .set({
        expires_at: response.expires_at.toISOString(),
        token: response.token,
      })
      .where(eq(verification_tokens.id, response.verification_id));

    this.logger.debug(
      { verificationId: response.verification_id },
      '[verifyCode] verified and issued token',
    );

    return response;
  }

  async verifyAndConsumeToken(tx: DrizzleDb, id: string, token: string) {
    const verificationToken = await tx.query.verification_tokens.findFirst({
      columns: { id: true, user_id: true },
      where: and(
        eq(verification_tokens.id, id),
        eq(verification_tokens.token, token),
        eq(verification_tokens.is_used, false),
        gt(verification_tokens.expires_at, new Date().toISOString()),
      ),
    });

    if (!verificationToken) {
      throw new UnauthorizedException(AUTH_ERROR.VERIFICATION_TOKEN_INVALID);
    }

    await tx
      .update(verification_tokens)
      .set({ is_used: true })
      .where(eq(verification_tokens.id, verificationToken.id));

    return verificationToken.user_id;
  }
  /*
 Prisma
  async verifyEmailVerificationToken(
    dto: IVerifyEmailQueryDto,
    reply: FastifyReply,
  ) {
    const now = new Date();
    const hashToken = await this.hashService.hashWithCrypto(dto.token);

    const verificationToken =
      await this.prismaService.verification_tokens.findFirst({
        select: { id: true, is_used: true, expires_at: true },
        where: {
          user_id: dto.userId,
          token: hashToken,
          expires_at: { gt: new Date() },
        },
      });

    if (!verificationToken) {
      return reply.type('text/html').send(
        renderTemplate(
          'verification-email-response',
          this.i18nService.translate('verify-email-response.invalid', {
            lang: dto.lang,
          }),
        ),
      );
    }

    if (verificationToken.is_used) {
      return reply.type('text/html').send(
        renderTemplate(
          'verification-email-response',
          this.i18nService.translate('verify-email-response.used', {
            lang: dto.lang,
          }),
        ),
      );
    }

    if (verificationToken.expires_at < now) {
      return reply.type('text/html').send(
        renderTemplate(
          'verification-email-response',
          this.i18nService.translate('verify-email-response.expired', {
            lang: dto.lang,
          }),
        ),
      );
    }

    const password = generateUniformStrongPassword();

    const hashedPassword = await this.hashService.hashWithBcrypt(password);

    const result = await this.prismaService.$transaction(async (tx) => {
      const updatedUser = await tx.users.update({
        select: {
          email: true,
          role: true,
          configurations: true,
          first_name: true,
          username: true,
        },
        where: { id: dto.userId, status: UserStatus.PENDING_VERIFICATION },
        data: { status: UserStatus.APPROVED, password: hashedPassword },
      });

      await tx.verification_tokens.update({
        where: { id: verificationToken.id },
        data: { is_used: true },
      });

      if (updatedUser.role === UserRole.ADMIN) {
        return {
          email: updatedUser.email,
          role: updatedUser.role,
          configurations: updatedUser.configurations,
          username: updatedUser.username,
        };
      } else {
        const [wholesalerUserId] = dto.token.split('@');

        const profile = await tx.users.findUnique({
          where: { user_id: wholesalerUserId },
          select: { profile: true },
        });

        const wholesalerProfile =
          profile?.profile as unknown as WholesalerProfileType;

        const companyName = wholesalerProfile.company_name || 'unknow';

        return {
          email: updatedUser.email,
          role: updatedUser.role,
          configurations: updatedUser.configurations,
          employeeName:
            updatedUser.first_name || updatedUser.username || updatedUser.email,
          companyName,
        };
      }
    });

    if (result.role === UserRole.ADMIN) {
      await this.mailService.sendActiveAdminWithTempPasswordEmail({
        to: result.email,
        adminName: result.username?.split('@')[1] || 'unknow',
        lang: result.configurations?.language,
        temporaryPassword: password,
      });
    } else {
      await this.mailService.sendActiveEmployeeWithTempPasswordEmail({
        to: result.email,
        lang: result.configurations?.language,
        employeeName: result.employeeName?.split('@')[1] || 'unknow',
        companyName: result.companyName || 'unknow',
        temporaryPassword: password,
      });
    }

    return reply.type('text/html').send(
      renderTemplate(
        'verification-email-response',
        this.i18nService.translate('verify-email-response.success', {
          lang: dto.lang,
        }),
      ),
    );
  }

*/
  async verifyEmailVerificationToken(
    dto: IVerifyEmailQueryDto,
    reply: FastifyReply,
  ) {
    const now = new Date();
    const hashToken = await this.hashService.hashWithCrypto(dto.token);

    const verificationToken =
      await this.drizzleService.db.query.verification_tokens.findFirst({
        columns: { id: true, is_used: true, expires_at: true },
        where: and(
          eq(verification_tokens.user_id, dto.userId),
          eq(verification_tokens.token, hashToken),
          gt(verification_tokens.expires_at, now.toISOString()),
        ),
      });

    if (!verificationToken) {
      return reply.type('text/html').send(
        renderTemplate(
          'verification-email-response',
          this.i18nService.translate('verify-email-response.invalid', {
            lang: dto.lang,
          }),
        ),
      );
    }

    if (verificationToken.is_used) {
      return reply.type('text/html').send(
        renderTemplate(
          'verification-email-response',
          this.i18nService.translate('verify-email-response.used', {
            lang: dto.lang,
          }),
        ),
      );
    }

    if (new Date(verificationToken.expires_at) < now) {
      return reply.type('text/html').send(
        renderTemplate(
          'verification-email-response',
          this.i18nService.translate('verify-email-response.expired', {
            lang: dto.lang,
          }),
        ),
      );
    }

    const password = generateUniformStrongPassword();

    const hashedPassword = await this.hashService.hashWithBcrypt(password);

    const result = await this.drizzleService.db.transaction(async (tx) => {
      // 定义更新用户的 CTE，并返回需要的字段
      const updatedUserCte = tx.$with('updated_user').as(
        tx
          .update(users)
          .set({ status: UserStatus.APPROVED, password: hashedPassword })
          .where(
            and(
              eq(users.id, dto.userId),
              eq(users.status, UserStatus.PENDING_VERIFICATION),
            ),
          )
          .returning({
            id: users.id,
            email: users.email,
            role: users.role,
            first_name: users.first_name,
            username: users.username,
          }),
      );
      // 主查询：从 CTE 中选择，并 LEFT JOIN configurations 表
      const [updatedUser] = await tx
        .with(updatedUserCte)
        .select({
          email: updatedUserCte.email,
          role: updatedUserCte.role,
          first_name: updatedUserCte.first_name,
          username: updatedUserCte.username,
          configurations: configurations,
        })
        .from(updatedUserCte)
        .leftJoin(configurations, eq(configurations.user_id, updatedUserCte.id))
        .limit(1);

      await tx
        .update(verification_tokens)
        .set({ is_used: true })
        .where(eq(verification_tokens.id, verificationToken.id));

      if (updatedUser.role === UserRole.ADMIN) {
        return {
          email: updatedUser.email,
          role: updatedUser.role,
          configurations: updatedUser.configurations,
          username: updatedUser.username,
        };
      } else {
        const [wholesalerUserId] = dto.token.split('@');

        const [profile] = await tx
          .select({ profile: users.profile })
          .from(users)
          .where(eq(users.user_id, wholesalerUserId));

        const wholesalerProfile =
          profile?.profile as unknown as WholesalerProfileType;

        const companyName = wholesalerProfile.company_name || 'unknow';

        return {
          email: updatedUser.email,
          role: updatedUser.role,
          configurations: updatedUser.configurations,
          employeeName:
            updatedUser.first_name || updatedUser.username || updatedUser.email,
          companyName,
        };
      }
    });

    if (result.role === UserRole.ADMIN) {
      await this.mailService.sendActiveAdminWithTempPasswordEmail({
        to: result.email,
        adminName: result.username?.split('@')[1] || 'unknow',
        lang: result.configurations?.language,
        temporaryPassword: password,
      });
    } else {
      await this.mailService.sendActiveEmployeeWithTempPasswordEmail({
        to: result.email,
        lang: result.configurations?.language,
        employeeName: result.employeeName?.split('@')[1] || 'unknow',
        companyName: result.companyName || 'unknow',
        temporaryPassword: password,
      });
    }

    return reply.type('text/html').send(
      renderTemplate(
        'verification-email-response',
        this.i18nService.translate('verify-email-response.success', {
          lang: dto.lang,
        }),
      ),
    );
  }
}
