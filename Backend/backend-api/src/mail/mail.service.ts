import { Injectable } from '@nestjs/common'; // 用于定义可注入的服务
import { ConfigService } from '@nestjs/config'; // 用于加载和管理应用程序的配置
import nodemailer from 'nodemailer'; // 引入 nodemailer 库，用于发送电子邮件


@Injectable()
export class MailService {
    private transporter: nodemailer.Transporter; // 定义一个 transporter 属性，用于发送邮件

    constructor(private config: ConfigService) {
        // 使用 ConfigService 获取环境变量中的 SMTP 配置
        this.transporter = nodemailer.createTransport({
            host: this.config.get<string>('SMTP_HOST')!,
            port: this.config.get<number>('SMTP_PORT')!,
            secure: false, // true for 465, false for other ports Gmail 官方推荐 587 + STARTTLS（对应 secure: false）
            auth: {
                user: this.config.get<string>('SMTP_USER')!, // SMTP 用户名
                pass: this.config.get<string>('SMTP_PASS')!, // SMTP 密码
            },
        });
    }

    // 发送验证邮件
    async sendVerificationEmail(to: string, token: string) {
        const link = 'prueba';

        const info = await this.transporter.sendMail({
            from: this.config.get<string>('SMTP_USER')!, // 发件人地址
            to, // 收件人地址
            subject: 'Email Verification', // 邮件主题
            text: `Please verify your email by clicking on the following link: ${link}`, // 邮件正文（纯文本）
            html: `<p>Please verify your email by clicking on the following link:</p><a href="${link}">${link}</a>`, // 邮件正文（HTML）
        });

        return info; // 返回发送邮件的信息
    }
}