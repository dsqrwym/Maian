import { Injectable } from '@nestjs/common'; // 用于定义可注入的服务
import { I18nTranslations } from '../i18n/generated/i18n.generated';
import { PinoLogger } from 'nestjs-pino';
import { I18nService } from 'nestjs-i18n';
import { maskEmail } from '../common/formatter/emial-format';
import { InjectQueue } from '@nestjs/bullmq';
import { JobsOptions, Queue } from 'bullmq';
import { ENV } from '../config/constants.config';
import { ConfigService } from '@nestjs/config';
import { RegisterEmailJob, ResetPasswordJob } from './mail.types';

@Injectable()
export class MailService {
  private readonly mailJobsOption: JobsOptions;
  constructor(
    private readonly logger: PinoLogger,
    private readonly i18nService: I18nService<I18nTranslations>,
    private readonly config: ConfigService,
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

  async sendNormalRegisterEmail(data: RegisterEmailJob) {
    const subject = this.i18nService.translate(
      'register-verification-email.subject',
      { lang: data.lang },
    );
    this.logger.info(
      `Queue job to send normal register email to ${maskEmail(data.to)} with subject: ${subject}`,
    );
    await this.mailQueue.add(
      'sendNormalRegisterEmail',
      data,
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
    const data: ResetPasswordJob = { user, code };
    await this.mailQueue.add('sendResetPassword', data, this.mailJobsOption);
    return { queued: true };
  }
}
