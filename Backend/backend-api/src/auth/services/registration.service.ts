import { BadRequestException, Injectable } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { MailService } from 'src/mail/mail.service';
import { PrismaService } from 'src/prisma/prisma.service';
import { RegisterDto } from '../dto/register.dto';
import { HashService } from 'src/common/hash/hash.service';
import { ConfigService } from '@nestjs/config';
import { Logger } from 'nestjs-pino';
import { Prisma, UserRole } from 'prisma/generated';
import { ENV } from '../../config/constants.config';
import { AUTH_ERROR } from '../auth.constants';
import { maskEmail } from '../../common/formatter/emial-format';

@Injectable()
export class RegistrationService {
  constructor(
    private readonly prismaService: PrismaService,
    private readonly hashService: HashService,
    private readonly jwtService: JwtService,
    private readonly configService: ConfigService,
    private readonly mailService: MailService,
    private readonly logger: Logger,
  ) {}
  async register(dto: RegisterDto) {
    const {
      email,
      password,
      username,
      firstName,
      lastName,
      phone,
      cif,
      role,
      profile,
      address,
      language,
      timezone,
    } = dto;
    this.logger.debug(
      { email: maskEmail(email) },
      '[Registration] Starting registration',
    );
    // 1. 检查用户是否已经存在
    const existingUser = await this.prismaService.users.findFirst({
      where: {
        OR: [{ email }, { username }],
      },
      select: { email: true, username: true },
    });

    if (existingUser) {
      if (existingUser.email === email) {
        this.logger.warn(
          { email: maskEmail(email) },
          '[Registration] Email conflict',
        );
        throw new BadRequestException(AUTH_ERROR.EMAIL_CONFLICT);
      }
      if (existingUser.username === username) {
        this.logger.warn({ username }, '[Registration] Username conflict');
        throw new BadRequestException(AUTH_ERROR.USERNAME_CONFLICT);
      }
    }

    // 2. 哈希密码
    const hashedPassword = await this.hashService.hashWithBcrypt(password); // 使用 bcrypt 哈希密码

    // 3. 开始事务
    return this.prismaService.$transaction(
      async (tx) => {
        const user = await tx.users.create({
          data: {
            email: email,
            username: username || null,
            password: hashedPassword,
            first_name: firstName || null,
            last_name: lastName || null,
            telephone: phone || null,
            role: role || UserRole.RETAILER, // 默认角色为 1 零售商
            cif: cif || null,

            profile: profile ? JSON.stringify(profile) : Prisma.JsonNull,

            configurations: {
              create: {
                language: language,
                timezone: timezone,
              },
            },

            direction: address
              ? {
                  createMany: {
                    data: address.map((a) => ({
                      type: a.type,
                      direction: a.direction,
                      city: a.city,
                      province: a.province,
                      zip_code: a.zip_code,
                      latitude: a.latitude,
                      longitude: a.longitude,
                    })),
                  },
                }
              : undefined,
          },
          include: { direction: true },
        });

        this.logger.debug({ userId: user.id }, '[Registration] User created');

        const mailToken = await this.jwtService.signAsync(
          { id: user.id },
          {
            expiresIn: '3 days',
          },
        ); // 生成 JWT token
        // 发送验证邮件
        this.logger.debug(
          { email: maskEmail(email) },
          '[Registration] Sending verification email',
        );
        this.mailService
          .sendVerificationEmail(email, mailToken, language)
          .catch((e: unknown) =>
            this.logger.error(
              { err: e, email: maskEmail(email) },
              '[Registration] Failed to send email',
            ),
          ); // 发送验证邮件

        if (profile && profile.licence) {
          delete profile.licence;
        }

        return {
          id: user.id,
          email: user.email,
          username: user.username,
          first_name: user.first_name,
          last_name: user.last_name,
          telephone: user.telephone,
          role: user.role,
          profile: profile,
        };
      },
      {
        maxWait:
          Number(this.configService.get<number>(ENV.PRISMA_MAX_WAIT)) || 5000,
        timeout:
          Number(this.configService.get<number>(ENV.PRISMA_TIMEOUT)) || 10000,
      },
    );
  }
}
