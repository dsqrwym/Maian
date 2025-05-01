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

@Injectable()
export class AuthService {
    constructor(
        private readonly configService: ConfigService,
        private readonly mailService: MailService,
        private readonly supabaseService: SupabaseService,
        private readonly prismaService: PrismaService,
        private readonly jwtService: JwtService,
        private readonly hashService: HashService,
    ) { }

    // 注册用户
    async register(dto: RegisterDto) {
        const { email, password, username, firstName, lastName, phone, role, language, timezone } = dto;
        // 1. 检查用户是否已经存在
        const [existingMail, existingUsername] = await Promise.all([
            this.prismaService.public_users.findUnique({ where: { email } }),
            username ? this.prismaService.public_users.findUnique({ where: { username } }) : null,
        ]);

        if (existingMail) {
            throw new BadRequestException('Email already exists');
        }

        if (existingUsername) {
            throw new BadRequestException('Username already exists');
        }

        // 3. 创建用户
        const hashedPassword = await this.hashService.hashWithBcrypt(password); // 使用 bcrypt 哈希密码
        const data = await this.supabaseService.createUser(email, hashedPassword);

        const user = await this.prismaService.public_users.create({
            data: {
                id: data.user.id,
                email: data.user.email || email,
                username: dto.username || email.split('@')[0], // 默认使用邮箱前缀作为用户名
                password: hashedPassword,
                first_name: dto.firstName || null,
                last_name: dto.lastName || null,
                telephone: dto.phone || null,
                role: dto.role || 1, // 默认角色为 1 零售商
            }
        });

        const mailToken = await this.jwtService.signAsync({ id: data.user.id }, {
            secret: this.configService.get<string>('MAIL_JWT_SECRET'), // 从配置服务中获取 JWT 密钥
            expiresIn: '3 days'
        }); // 生成 JWT token
        // 5. 发送验证邮件

        this.mailService.sendVerificationEmail(email, mailToken, dto.language, dto.timezone, user.created_at); // 发送验证邮件
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
                where: { id: userId }
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