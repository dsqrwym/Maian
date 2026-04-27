import { Injectable } from '@nestjs/common'; // 用于定义可注入的服务
import { I18nTranslations } from '#/i18n/generated/i18n.generated.js';
import { PinoLogger } from 'nestjs-pino';
import { I18nService } from 'nestjs-i18n';
import { maskEmail } from '#/utils/email.utils.js';
import { InjectQueue } from '@nestjs/bullmq';
import { JobsOptions, Queue } from 'bullmq';
import { ENV } from '#/config/constants.config.js';
import { ConfigService } from '@nestjs/config';
import {
  ActiveAdminWithPasswordEmailJob,
  ActiveEmployeeWithPasswordEmailJob,
  BaseEmailJobWithLink,
  RegisterEmailJob,
  ResetPasswordJob,
  VerifyEmployeeEmailJob,
} from './mail.types.js';

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
      attempts: Number(this.config.get<number>(ENV.SMTP_RETRIES, 3)),
      backoff: {
        type: 'fixed',
        delay: Number(this.config.get<number>(ENV.SMTP_DELAY_TIME, 60000)),
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

  async sendResetPassword(dto: ResetPasswordJob) {
    const lang = dto.lang || 'en';
    const subject: string = this.i18nService.translate(
      'reset-password.resetPasswordSubject',
      { lang },
    );
    this.logger.info(
      `Queue job to send reset password email to ${maskEmail(dto.to)} with subject: ${subject}`,
    );
    await this.mailQueue.add('sendResetPassword', dto, this.mailJobsOption);
    return { queued: true };
  }

  async sendEmployeeVerifyEmail(dto: VerifyEmployeeEmailJob) {
    const lang = dto.lang || 'en';
    const subject = this.i18nService.translate(
      'register-verification-email.subject',
      { lang },
    );
    this.logger.info(
      `Queue job to send employee verify email to ${maskEmail(dto.to)} with subject: ${subject}`,
    );
    await this.mailQueue.add(
      'sendVerifyEmployeeEmail',
      dto,
      this.mailJobsOption,
    );
    return { queued: true };
  }

  async sendAdminVerifyEmail(dto: BaseEmailJobWithLink) {
    const lang = dto.lang || 'en';
    const subject = this.i18nService.translate(
      'register-verification-email.subject',
      { lang },
    );
    this.logger.info(
      `Queue job to send admin verify email to ${maskEmail(dto.to)} with subject: ${subject}`,
    );
    await this.mailQueue.add('sendVerifyAdminEmail', dto, this.mailJobsOption);
    return { queued: true };
  }

  async sendActiveEmployeeWithTempPasswordEmail(
    dto: ActiveEmployeeWithPasswordEmailJob,
  ) {
    const lang = dto.lang || 'en';
    const subject = this.i18nService.translate('employee-activation.subject', {
      lang,
    });
    this.logger.info(
      `Queue job to send employee verify email to ${maskEmail(dto.to)} with subject: ${subject}`,
    );
    await this.mailQueue.add(
      'sendActiveEmployeeWithTempPassword',
      dto,
      this.mailJobsOption,
    );
    return { queued: true };
  }

  async sendActiveAdminWithTempPasswordEmail(
    dto: ActiveAdminWithPasswordEmailJob,
  ) {
    const lang = dto.lang || 'en';
    const subject = this.i18nService.translate('admin-activation.subject', {
      lang,
    });
    this.logger.info(
      `Queue job to send admin verify email to ${maskEmail(dto.to)} with subject: ${subject}`,
    );
    await this.mailQueue.add(
      'sendActiveAdminWithTempPassword',
      dto,
      this.mailJobsOption,
    );
    return { queued: true };
  }
}
