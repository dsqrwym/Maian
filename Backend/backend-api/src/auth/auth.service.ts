import { BadRequestException, Injectable, UnauthorizedException } from '@nestjs/common';
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


interface LoginTokenPayload {
    userId: string,
    userRole: number,
    deviceFinger: string
}

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
    // 生成Auth Token
    generateToken = async (payload: LoginTokenPayload, expiresIn: string) => {
        return this.jwtService.signAsync(payload, {
            secret: this.configService.get<string>('SUPABASE_JWT_SECRET'),
            expiresIn,
        });
    };

    // 注册用户
    async register(dto: RegisterDto) {
        const { email, password, username, firstName, lastName, phone, cif, role, profile, address, language, timezone } = dto;
        this.logger.debug(`[Registration] Starting registration for: ${email}`);
        // 1. 检查用户是否已经存在
        const existingUser = await this.prismaService.public_users.findFirst({
            where: {
                OR: [{ email }, { username }],
            },
            select: { email: true, username: true },
        });

        if (existingUser) {
            if (existingUser.email === email) {
                this.logger.warn(`[Registration] Email conflict: ${email}`);
                throw new BadRequestException('Email already exists');
            }
            if (existingUser.username === username) {
                this.logger.warn(`[Registration] Username conflict: ${username}`);
                throw new BadRequestException('Username already exists');
            }
        }

        // 2. 哈希密码
        const hashedPassword = await this.hashService.hashWithBcrypt(password); // 使用 bcrypt 哈希密码

        // 3. 开始事务
        return await this.prismaService.$transaction(async () => {
            const data = await this.supabaseService.createUser(email, hashedPassword);

            if (!data.user) return;

            const user = await this.prismaService.public_users.create({
                data: {
                    id: data.user.id,
                    email: data.user.email || email,
                    username: username || null,
                    password: hashedPassword,
                    first_name: firstName || null,
                    last_name: lastName || null,
                    telephone: phone || null,
                    role: role || 1, // 默认角色为 1 零售商
                    cif: cif || null,
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
        const { email, username, deviceName, ipAddress, userAgent } = dto;

        if (!email && !username) {
            throw new BadRequestException("email or username doesn't exist!");
        }

        // 查找用户
        const user = await this.prismaService.public_users.findFirst({
            where: {
                OR: [{ username }, { email }],
            },
        });

        if (!user) {
            throw new BadRequestException('User does not exist!');
        }

        // 验证密码
        if (!await this.hashService.compareWithBcrypt(dto.password, user.password)) {
            throw new BadRequestException('Incorrect password');
        }

        // 生成设备哈希
        const deviceHash = (await this.hashService.hashWithCrypto(deviceName + userAgent));

        // 生成 token payload
        const payload: LoginTokenPayload = {
            userId: user.id,
            userRole: user.role,
            deviceFinger: deviceHash
        };

        // 查找是否已存在相同设备的登录记录
        const existingSession = await this.prismaService.user_sessions.findUnique({
            where: { user_id_device_finger: { user_id: user.id, device_finger: deviceHash } },
            select: { token_id: true }
        });

        const refreshToken = await this.generateToken(payload, '7 days');
        const accessToken = await this.generateToken(payload, '1h');

        if (!existingSession) {
            // 如果是新设备登录，保存会话记录
            await this.prismaService.user_sessions.create({
                data: {
                    user_id: user.id,
                    device_name: deviceName,
                    device_finger: deviceHash,
                    user_agent: userAgent,
                    last_ip: ipAddress,
                    access_token: accessToken,
                    refresh_token: refreshToken,
                },
            });
        } else {
            // 更新会话记录
            await this.prismaService.user_sessions.update({
                where: { user_id_device_finger: { user_id: user.id, device_finger: deviceHash } },
                data: {
                    access_token: accessToken,
                    refresh_token: refreshToken,
                    last_ip: ipAddress,
                    last_active: new Date(),
                },
            });
        }

        return {
            accessToken,
            refreshToken,
        };
    }

    async getAccessToken(refreshToken: string) {
        try {
            const payload: LoginTokenPayload = await this.jwtService.verifyAsync(refreshToken, {
                secret: this.configService.get<string>('SUPABASE_JWT_SECRET'),
            });

            const session = await this.prismaService.user_sessions.findUnique({
                where: {
                    user_id_device_finger: {
                        user_id: payload.userId,
                        device_finger: payload.deviceFinger,
                    },
                },
                select: {
                    refresh_token: true,
                    revoked: true
                }
            });

            if (!session || session.revoked || session.refresh_token !== refreshToken) {
                throw new UnauthorizedException('Session invalid or revoked');
            }

            const newAccessToken = await this.generateToken(payload, '1h');

            // 更新 last_active
            await this.prismaService.user_sessions.update({
                where: {
                    user_id_device_finger: {
                        user_id: payload.userId,
                        device_finger: payload.deviceFinger,
                    },
                },
                data: {
                    last_active: new Date(),
                    access_token: newAccessToken,
                },
            });

            return {
                accessToken: newAccessToken,
            };

        } catch (err) {
            throw new UnauthorizedException('Invalid or expired refresh token : ' + err);
        }
    }
}