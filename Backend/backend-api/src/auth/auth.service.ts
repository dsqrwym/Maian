import { BadRequestException, Injectable } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { MailService } from 'src/mail/mail.service';
import { PrismaService } from 'src/prisma/prisma.service';
import { SupabaseService } from 'src/supabase/supabase.service';
import { RegisterDto } from './dto/register.dto';
import { HashService } from 'src/common/services/hash.service';
import { ConfigService } from '@nestjs/config';




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
        const { email, password } = dto;
        // 1. 检查用户是否已经存在
        const existingMail = await this.prismaService.public_users.findUnique({
            where: { email },
        });

        if (existingMail) {
            throw new BadRequestException('Mail already exists');
        }
        // 2. 检查用户名是否已经存在
        if (dto.username) {
            const existingUsername = await this.prismaService.public_users.findUnique({
                where: { username: dto.username },
            });
            if (existingUsername) {
                throw new BadRequestException('Username already exists');
            }
        }

        // 3. 创建用户
        const hashedPassword = await this.hashService.hashString(password); // 使用 bcrypt 哈希密码
        const data = await this.supabaseService.createUser(email, hashedPassword);

        this.prismaService.public_users.create({
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

        const mailJWTPayload = {
            id: data.user.id
        }
        const mailToken = await this.jwtService.signAsync(mailJWTPayload, {
            secret: this.configService.get<string>('MAIL_JWT_SECRET'), // 从配置服务中获取 JWT 密钥
            expiresIn: '3 days'
        }); // 生成 JWT token
        // 5. 发送验证邮件

    }


    async verifyEmail(token: string) {
        const payload = await this.jwtService.verifyAsync(token, {
            secret: this.configService.get<string>('MAIL_JWT_SECRET'), // 从配置服务中获取 JWT 密钥
        });
        const userId = payload.id; // 获取用户 ID

        // 6. 更新用户的邮箱验证状态
        await this.prismaService.public_users.update({
            where: { id: userId },
            data: { status: 1 }, // 1 激活
        });

        return { message: 'Email verified successfully' };
    }
}