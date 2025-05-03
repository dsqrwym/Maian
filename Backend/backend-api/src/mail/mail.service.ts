import { Inject, Injectable, InternalServerErrorException } from '@nestjs/common'; // 用于定义可注入的服务
import { ConfigService } from '@nestjs/config'; // 用于加载和管理应用程序的配置
import * as nodemailer from 'nodemailer'; // 引入 nodemailer 库，用于发送电子邮件

import { REQUEST } from '@nestjs/core'; // 用于获取当前请求对象
import { FastifyRequest } from 'fastify'; // 引入 FastifyRequest 类型

import { DateFormatService } from 'src/common/services/date-format.service';
import { VerificationContent } from './templates/verification-content'; // 引入邮件模板 内容
import { getVerificationEmailHtml } from './templates/verification.templates'; // 引入邮件模板 HTML 
import { PinoLogger } from 'nestjs-pino';

@Injectable()
export class MailService {
    private readonly transporter: nodemailer.Transporter; // 定义一个 transporter 属性，用于发送邮件
    private readonly retries: number;
    private readonly deleyTime: number; // 单位为毫秒

    constructor(
        private readonly logger: PinoLogger,
        private config: ConfigService,
        private dateFormatService: DateFormatService,
        @Inject(REQUEST) private readonly request: FastifyRequest
    ) { // 注入当前请求对象) {
        // 使用 ConfigService 获取环境变量中的 SMTP 配置
        this.transporter = nodemailer.createTransport({
            host: this.config.get<string>('SMTP_HOST')!,
            port: Number(this.config.get<number>('SMTP_PORT'))!,
            secure: false, // true for 465, false for other ports Gmail 官方推荐 587 + STARTTLS（对应 secure: false）
            auth: {
                user: this.config.get<string>('SMTP_USER')!, // SMTP 用户名
                pass: this.config.get<string>('SMTP_PASS')!, // SMTP 密码
            },
        });
        this.retries = this.config.get<number>('SMTP_RETRIES') || 3; // 获取重试次数，默认为 3 次
        this.deleyTime = this.config.get<number>('SMTP_DELAY_TIME') || 60000; // 获取延迟时间，默认为 1 分钟
    }

    private delay(ms: number) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    // 发送验证邮件
    async sendVerificationEmail(to: string, token: string, lang: string = 'en', timezone: string = 'UTC', registerDate: Date) {
        const protocol = this.request.protocol; // 获取请求的协议（http 或 https）
        const host = this.request.headers.host || this.request.hostname; // 获取请求的主机名（如：localhost:3000 或 yourdomain.com）

        const link = `${protocol}://${host}/api/auth/verify-email?lang=${lang}&token=${token}`; // 构建验证链接
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

        let attempts = 0; // 初始化尝试次数
        while (attempts < this.retries) { // 循环直到达到最大重试次数
            try {
                // 记录邮件发送尝试
                this.logger.info(`Attempt ${attempts + 1} to send verification email to ${to}`);

                const info = await this.transporter.sendMail({
                    from: this.config.get<string>('SMTP_USER')!, // 发件人地址
                    to, // 收件人地址
                    subject: verificationContent.title, // 邮件主题
                    html: html, // 邮件正文（HTML）
                });

                this.logger.info(`Email sent successfully to ${to} with info: ${JSON.stringify(info)}`);

                return info; // 返回发送邮件的信息
            } catch (error) {
                attempts++; // 增加尝试次数
                // 记录错误信息并处理重试
                this.logger.error(`Attempt ${attempts} to send email failed: ${error.message}`);

                if (attempts >= this.retries) { // 如果达到最大重试次数，抛出错误
                    this.logger.error(`Failed to send email after ${this.retries} attempts`);
                    throw new InternalServerErrorException(`Failed to send email after ${this.retries} attempts: ${error}`);
                } else {
                    await this.delay(this.deleyTime); // 等待 60 秒后重试
                }
                this.logger.error(`Attempt ${attempts} failed: ${error}`); // 打印错误信息
            }
        }
    }

    async sendNotifyEmail(to: string, sesion: any, loginDate: Date){
        
    }
}