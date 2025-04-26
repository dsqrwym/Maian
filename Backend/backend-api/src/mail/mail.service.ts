import { Injectable } from '@nestjs/common'; // 用于定义可注入的服务
import { ConfigService } from '@nestjs/config'; // 用于加载和管理应用程序的配置
import nodemailer from 'nodemailer'; // 引入 nodemailer 库，用于发送电子邮件

import { DateFormatService } from 'src/common/services/date-format.service';
import { VerificationContent } from './templates/verification-content'; // 引入邮件模板 内容
import { getVerificationEmailHtml } from './templates/verification.templates'; // 引入邮件模板 HTML 

@Injectable()
export class MailService {
    private transporter: nodemailer.Transporter; // 定义一个 transporter 属性，用于发送邮件

    constructor(private config: ConfigService, private dateFormatService: DateFormatService) {
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
    async sendVerificationEmail(to: string, token: string, lang: string = 'en', timezone: string = 'UTC', registerDate: Date) {
        const link = 'prueba';
        const verificationContent = VerificationContent(this.dateFormatService, lang, timezone, registerDate); // 获取邮件内容
        const html = getVerificationEmailHtml({
            title: verificationContent.title,
            hello: verificationContent.hello,
            thankRegister: verificationContent.thankRegister,
            reminderVerification: verificationContent.reminderVerification,
            content: verificationContent.content,
            repeatReminder: verificationContent.repeatReminder,
            buttonText: verificationContent.buttonText,
            support: verificationContent.support,
            notReply: verificationContent.notReply,
            buttonLink: link
        }); // 获取邮件 HTML 内容

        const info = await this.transporter.sendMail({
            from: this.config.get<string>('SMTP_USER')!, // 发件人地址
            to, // 收件人地址
            subject: verificationContent.title, // 邮件主题
            html: html, // 邮件正文（HTML）
        });

        return info; // 返回发送邮件的信息
    }
}