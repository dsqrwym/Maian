import {
  Injectable,
  NotFoundException,
  UnauthorizedException,
} from '@nestjs/common';
import { Logger } from 'nestjs-pino';
import { PrismaService } from 'src/prisma/prisma.service';
import { Prisma, UserRole, UserStatus } from '@prisma/client';
import {
  SendVerificationCodeDto,
  VerifyCodeDto,
  VerifyCodeResponseDto,
  VerifyEmailQueryDto,
} from '../dto/verification.dto';
import { maskEmail } from '../../common/formatter/emial-format';
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

@Injectable()
export class VerificationService {
  constructor(
    private readonly i18nService: I18nService,
    private readonly prismaService: PrismaService,
    private readonly mailService: MailService,
    private readonly hashService: HashService,
    private readonly logger: Logger,
  ) {}

  async sendVerificationCode(
    sendVerificationDto: SendVerificationCodeDto,
    emailType: VerificationEmailType,
  ) {
    const email = sendVerificationDto.email;
    const markedEmail = maskEmail(email);
    this.logger.debug({ email: markedEmail }, '[sendVerificationCode] start');
    const user = await this.prismaService.users.findUnique({
      where: { email: email },
      select: {
        id: true,
        username: true,
        configurations: { select: { language: true } },
        verification_tokens: {
          where: { created_at: { gt: addMinutes(new Date(), -1) } },
          take: 1,
          select: { id: true },
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

    await this.prismaService.verification_tokens.create({
      data: {
        user_id: user.id,
        token: hashedCode,
        expires_at: addMinutes(new Date(), 10), // 10 分钟过期
      },
    });

    switch (emailType) {
      case VerificationEmailType.NORMAL_REGISTER: {
        const registerEmailJob: RegisterEmailJob = {
          to: email,
          lang: user.configurations?.language,
          link: sendVerificationDto.deepLink,
          code: code,
        };
        await this.mailService.sendNormalRegisterEmail(registerEmailJob);
        break;
      }
      case VerificationEmailType.RESET_PASSWORD:
        await this.mailService.sendResetPassword({
          to: email,
          name: user.username ?? email,
          lang: user.configurations?.language,
          code: code,
        });
        break;
    }

    this.logger.debug(
      { userId: user.id, email: markedEmail },
      `[sendVerificationCode] sent , language${user.configurations?.language}`,
    );
  }

  async verifyCode(verifyCode: VerifyCodeDto, expiresMinutes: number = 10) {
    const [userCode] = await Promise.all([
      this.prismaService.users.findUnique({
        where: {
          email: verifyCode.email,
        },
        select: {
          verification_tokens: {
            where: {
              is_used: false,
              expires_at: { gt: new Date() },
            },
            select: { token: true, id: true, attempts: true, expires_at: true },
            orderBy: { created_at: 'desc' },
            take: 1,
          },
        },
      }),
    ]);

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
      const updatedToken = await this.prismaService.verification_tokens.update({
        where: { id: userCode.verification_tokens[0].id },
        data: { attempts: { increment: 1 } },
        select: { attempts: true },
      });
      if (updatedToken.attempts >= 3) {
        await this.prismaService.verification_tokens.update({
          where: { id: userCode.verification_tokens[0].id },
          data: { is_used: true },
        });
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

    await this.prismaService.verification_tokens.update({
      where: {
        id: response.verification_id,
      },
      data: {
        expires_at: response.expires_at,
        token: response.token,
      },
    });

    this.logger.debug(
      { verificationId: response.verification_id },
      '[verifyCode] verified and issued token',
    );

    return response;
  }

  async verifyAndConsumeToken(
    tx: Prisma.TransactionClient,
    id: string,
    token: string,
  ) {
    const verificationToken = await tx.verification_tokens.findFirst({
      where: { id, token, is_used: false, expires_at: { gt: new Date() } },
      select: { id: true, user_id: true },
    });

    if (!verificationToken) {
      throw new UnauthorizedException(AUTH_ERROR.VERIFICATION_TOKEN_INVALID);
    }

    await tx.verification_tokens.update({
      where: { id: verificationToken.id },
      data: { is_used: true },
    });

    return verificationToken.user_id;
  }

  async verifyEmailVerificationToken(
    dto: VerifyEmailQueryDto,
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
}
