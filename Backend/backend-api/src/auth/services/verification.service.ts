import {
  Injectable,
  NotFoundException,
  UnauthorizedException,
} from '@nestjs/common';
import { Logger } from 'nestjs-pino';
import { PrismaService } from 'src/prisma/prisma.service';
import { Prisma } from 'prisma/generated';
import {
  SendVerificationCodeDto,
  VerifyCodeDto,
  VerifyCodeResponseDto,
} from '../dto/verification.dto';
import { maskEmail } from '../../common/formatter/emial-format';
import { addMinutes } from '../../utils/date.utils';
import { AUTH_ERROR } from '../auth.constants';
import { TooManyRequestsExceptions } from '../../common/exceptions/too-many-requests.exceptions';
import { generateUniformRandomDigits } from '../../utils/random.utils';
import { MailService } from '../../mail/mail.service';
import { HashService } from 'src/common/hash/hash.service';
import { VerificationEmailType } from '../auth.types';
import { randomUUID } from 'node:crypto';
import { RegisterEmailJob } from '../../mail/mail.types';

@Injectable()
export class VerificationService {
  constructor(
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
        await this.mailService.sendResetPassword(
          {
            email: email,
            name: user.username ?? email,
            language: user.configurations?.language,
          },
          code,
        );
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
      throw new NotFoundException(AUTH_ERROR.VERIFICATION_TOKEN_INVALID);
    }

    await tx.verification_tokens.update({
      where: { id: verificationToken.id },
      data: { is_used: true },
    });

    return verificationToken.user_id;
  }
}
