import { Injectable } from '@nestjs/common';
import { I18nService } from 'nestjs-i18n';
import { PinoLogger } from 'nestjs-pino';
import { maskEmail } from '../../common/formatter/emial-format';
import { MailerService } from '@nestjs-modules/mailer';
import { I18nTranslations } from '../../i18n/generated/i18n.generated';
import { VerificationEmailJob } from '../mail.types';

@Injectable()
export class VerificationMailProcessorService {
  constructor(
    private readonly mailerService: MailerService,
    private readonly logger: PinoLogger,
    private readonly i18nService: I18nService<I18nTranslations>,
  ) {}
  // 发送验证邮件
  private async sendVerificationEmail(
    data: VerificationEmailJob,
    subject: string,
    title: string,
    content: string,
    content2: string,
    button: string,
    link: string,
    ignore: string,
    support: string,
  ) {
    try {
      const info: unknown = await this.mailerService.sendMail({
        to: data.to,
        subject,
        template: 'verification-email',
        context: { title, content, content2, button, link, ignore, support },
      });
      this.logger.info(
        `Email sent successfully to ${maskEmail(data.to)} with info: ${JSON.stringify(info)}`,
      );
      return info;
    } catch (err) {
      const error = err instanceof Error ? err : new Error(String(err));
      this.logger.error(
        `sendVerification failed: ${error.message}`,
        error.stack,
      );
      throw error; // 让 BullMQ 来 retry
    }
  }

  async sendVerification(data: VerificationEmailJob) {
    const lang = data.lang || 'en';
    const link = data.link;

    if (!data.to) {
      this.logger.error('sendVerificationEmail missing recipient');
      return;
    }
    if (!link) {
      this.logger.error(
        'sendVerificationEmail missing link or verifyBaseUrl+token',
      );
      return;
    }

    const subject = this.i18nService.translate('verification-email.subject', {
      lang,
    });
    const title = this.i18nService.translate('verification-email.title', {
      lang,
    });
    const content = this.i18nService.translate('verification-email.content', {
      lang,
    });
    const content2 = this.i18nService.translate('verification-email.content2', {
      lang,
    });
    const button = this.i18nService.translate('verification-email.button', {
      lang,
    });
    const ignore = this.i18nService.translate('verification-email.ignore', {
      lang,
    });
    const support = this.i18nService.translate('verification-email.support', {
      lang,
    });

    return this.sendVerificationEmail(
      data,
      subject,
      title,
      content,
      content2,
      button,
      link,
      ignore,
      support,
    );
  }

  async repeatVerificationEmail(data: VerificationEmailJob) {
    const lang = data.lang || 'en';
    const link = data.link;
    const timeZone = data.timeZone || 'UTC';

    if (!data.to) {
      this.logger.error('sendVerificationEmail missing recipient');
      return;
    }
    if (!link) {
      this.logger.error(
        'sendVerificationEmail missing link or verifyBaseUrl+token',
      );
      return;
    }

    const subject = this.i18nService.translate('verification-email.subject', {
      lang,
    });
    const title = this.i18nService.translate('verification-email.title', {
      lang,
    });
    const content = this.i18nService.translate(
      'repeat-verification-email.content',
      {
        lang,
      },
    );
    const content2 = this.i18nService.translate(
      'repeat-verification-email.content2',
      {
        lang,
        args: { date: data.date.toLocaleString(lang, { timeZone: timeZone }) },
      },
    );
    const button = this.i18nService.translate('verification-email.button', {
      lang,
    });
    const ignore = this.i18nService.translate('verification-email.ignore', {
      lang,
    });
    const support = this.i18nService.translate('verification-email.support', {
      lang,
    });

    return this.sendVerificationEmail(
      data,
      subject,
      title,
      content,
      content2,
      button,
      link,
      ignore,
      support,
    );
  }
}
