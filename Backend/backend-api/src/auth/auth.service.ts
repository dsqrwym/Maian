import { BadRequestException, Injectable } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { MailService } from 'src/mail/mail.service';
import { PrismaService } from 'src/prisma/prisma.service';
import { SupabaseService } from 'src/supabase/supabase.service';
import { RegisterDto } from './dto/register.dto';
import { HashService } from 'src/common/services/hash.service';
import { ConfigService } from '@nestjs/config';
import { LoginDto } from './dto/login.dto';
import { FastifyReply } from 'fastify';

import { getVerificationResponseContent } from 'src/mail/templates/varification-response-content';
import { getVerificationResponseHtml } from 'src/mail/templates/verification-response.tmplates';
import { Logger } from 'nestjs-pino';
import { Prisma } from 'prisma/generated/prisma';

@Injectable()
export class AuthService {
    constructor(
        private readonly configService: ConfigService,
        private readonly mailService: MailService,
        private readonly supabaseService: SupabaseService,
        private readonly prismaService: PrismaService,
        private readonly jwtService: JwtService,
        private readonly hashService: HashService,
        private readonly logger: Logger
    ) { }

    private async generateUniqueUsername(base: string): Promise<string> {
        const maxLength = 30;
        const maxTries = 3;

        const trimBase = (str: string) => str.slice(0, maxLength - 5); // 留出 5 位后缀
        const suffix = () => Math.floor(10000 + Math.random() * 90000); // 生成 10000~99999 的 5 位数

        base = trimBase(base);
        let tries = 0;
        let username: string;

        do {
            if (tries >= maxTries) {
                // fallback：使用 uuid 前缀，保证唯一且短
                return crypto.randomUUID().replace(/-/g, '').slice(0, maxLength);
            }

            username = `${base}${suffix()}`;
            tries++;
        } while (await this.prismaService.public_users.findUnique({ where: { username }, select: { id: true } }));

        return username;
    }


    // 注册用户
    async register(dto: RegisterDto) {
        const { email, password, username, firstName, lastName, phone, role, profile, address, language, timezone } = dto;
        this.logger.debug(`[Registration] Starting registration for: ${email}`);
        // 1. 检查用户是否已经存在
        const [existingMail, existingUsername] = await Promise.all([
            this.prismaService.public_users.findUnique({
                where: { email },
                select: { id: true }
            }),
            username ? this.prismaService.public_users.findUnique({
                where: { username },
                select: { id: true }
            }) : Promise.resolve(null)
        ]);

        if (existingMail) {
            this.logger.warn(`[Registration] Email conflict: ${email}`);
            throw new BadRequestException('Email already exists');
        }

        if (existingUsername) {
            this.logger.warn(`[Registration] Username conflict: ${username}`);
            throw new BadRequestException('Username already exists');
        }

        // 3. 创建用户
        const hashedPassword = await this.hashService.hashWithBcrypt(password); // 使用 bcrypt 哈希密码
        return await this.prismaService.$transaction(async () => {
            const data = await this.supabaseService.createUser(email, hashedPassword);

            if (!data.user) return;

            const finalUsername = username || await this.generateUniqueUsername(email.split('@')[0]);

            const user = await this.prismaService.public_users.create({
                data: {
                    id: data.user.id,
                    email: data.user.email || email,
                    username: finalUsername,
                    password: hashedPassword,
                    first_name: firstName || null,
                    last_name: lastName || null,
                    telephone: phone || null,
                    role: role || 1, // 默认角色为 1 零售商
                    profile: profile ? JSON.stringify(profile) : Prisma.JsonNull,

                    direcction: address ? {
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
                    } : undefined,
                },
                include: { direcction: true }
            });

            this.logger.debug(`[Registration] User created: ${user.id}`);

            const mailToken = await this.jwtService.signAsync({ id: data.user.id }, {
                secret: this.configService.get<string>('MAIL_JWT_SECRET'), // 从配置服务中获取 JWT 密钥
                expiresIn: '3 days'
            }); // 生成 JWT token
            // 发送验证邮件
            this.logger.debug(`Sending verification email to: ${email}`);
            this.mailService.sendVerificationEmail(email, mailToken, language, timezone, user.created_at).catch(e =>
                this.logger.error(`[Registration] Failed to send email: ${e.message}`)
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
                profile: profile
            };
        },
            {
                maxWait: Number(this.configService.get<number>('PRISMA_MAX_WAIT')) || 5000,
                timeout: Number(this.configService.get<number>('PRISMA_TIMEOUT')) || 10000
            });
    }


    async verifyEmail(token: string, lang: string, reply: FastifyReply) {
        const sendHtml = (key: 'invalid' | 'alreadyVerified' | 'success') => {
            const content = getVerificationResponseContent(lang)[key];
            return reply.type('text/html').send(getVerificationResponseHtml(content));
        };

        try {
            const payload = await this.jwtService.verifyAsync(token, {
                secret: this.configService.get<string>('MAIL_JWT_SECRET'),
            });

            const userId = payload.id;
            if (!userId) return sendHtml('invalid');

            const user = await this.prismaService.public_users.findUnique({
                where: { id: userId },
                select: { status: true }
            });

            if (!user) return sendHtml('invalid');
            if (user.status === 1) return sendHtml('alreadyVerified');

            await this.prismaService.public_users.update({
                where: { id: userId },
                data: { status: 1 },
            });

            return sendHtml('success');

        } catch (error) {
            return sendHtml('invalid');
        }
    }


    async login(dto: LoginDto) {
        const { email, username } = dto

        if (!email && !username) {
            throw new BadRequestException("email o username d'ont exist!");
        }

        const user = await this.prismaService.public_users.findUnique({ where: { username } }) || await this.prismaService.public_users.findUnique({ where: { email } });

        if (!user) {
            throw new BadRequestException('User do not exist!');
        }

        if (!await this.hashService.compareWithBcrypt(dto.password, user.password)) {
            throw new BadRequestException('Incorrect password');
        }

        const payload = {
            userId: user.id,
            userRole: user.role
        }
        const refreshToken = await this.jwtService.signAsync(payload, {
            secret: this.configService.get<string>('SUPABASE_JWT_SECRET'),
            expiresIn: '3 days'
        });

    }


    async getRefreshToken() { }
}