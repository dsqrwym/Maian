import { Inject, Injectable } from '@nestjs/common'; // 用于定义可注入的服务
import { REQUEST } from '@nestjs/core'; // 用于获取当前请求对象
import { FastifyRequest } from 'fastify'; // 引入 FastifyRequest 类型

import { I18nTranslations } from '../i18n/generated/i18n.generated';
import { PinoLogger } from 'nestjs-pino';
import { I18nService, TranslateOptions } from 'nestjs-i18n';
import { maskEmail } from '../common/formatter/emial-format';
import { InjectQueue } from '@nestjs/bullmq';
import { JobsOptions, Queue } from 'bullmq';
import { ENV } from '../config/constants.config';
import { ConfigService } from '@nestjs/config';

@Injectable()
export class MailService {
  private readonly mailJobsOption: JobsOptions;
  constructor(
    private readonly logger: PinoLogger,
    private readonly i18nService: I18nService<I18nTranslations>,
    private readonly config: ConfigService,
    @Inject(REQUEST) private readonly request: FastifyRequest,
    @InjectQueue('mail') private readonly mailQueue: Queue,
  ) {
    this.mailJobsOption = {
      attempts: this.config.get<number>(ENV.SMTP_RETRIES, 3),
      backoff: {
        type: 'fixed',
        delay: this.config.get<number>(ENV.SMTP_DELAY_TIME, 60000),
      }, // 每次失败后延迟 60s
      removeOnComplete: true,
      removeOnFail: false,
    };
  }

  // 发送验证邮件
  async sendVerificationEmail(to: string, token: string, lang: string = 'en') {
    const protocol = this.request.protocol;
    const host = this.request.headers.host || this.request.hostname;
    const link = `${protocol}://${host}/api/auth/verify-email?lang=${lang}&token=${token}`;
    const translationOption: TranslateOptions = { lang };
    const subject = this.i18nService.translate(
      'verification-email.subject',
      translationOption,
    );
    // 仅用于日志，真正的模板填充在处理器里完成
    this.logger.info(
      `Queue job to send verification email to ${maskEmail(to)} with subject: ${subject}`,
    );
    await this.mailQueue.add(
      'sendVerificationEmail',
      { to, lang, link },
      this.mailJobsOption,
    );
    return { queued: true };
  }

  async sendResetPassword(
    user: { email: string; name: string; language?: string },
    code: string,
  ) {
    const lang = user.language || 'en';
    const namespace = 'reset-password.';
    const subject: string = this.i18nService.translate(
      `${namespace}resetPasswordSubject`,
      { lang },
    );
    this.logger.info(
      `Queue job to send reset password email to ${maskEmail(user.email)} with subject: ${subject}`,
    );
    await this.mailQueue.add(
      'sendResetPassword',
      { user, code },
      this.mailJobsOption,
    );
    return { queued: true };
  }
}
