import { Injectable } from '@nestjs/common';
import { I18nService } from 'nestjs-i18n';
import { PinoLogger } from 'nestjs-pino';
import { MailerService } from '@nestjs-modules/mailer';
import { I18nTranslations } from '@/i18n/generated/i18n.generated';
import { RegisterEmailJob } from '../mail.types';
import { sendMail } from '@/utils/mailer.utils';

@Injectable()
export class VerifyRegistrationProcessorService {
  constructor(
    private readonly mailerService: MailerService,
    private readonly logger: PinoLogger,
    private readonly i18nService: I18nService<I18nTranslations>,
  ) {}

  async sendNormalRegisterEmail(data: RegisterEmailJob) {
    const lang = data.lang || 'en';
    const subject = this.i18nService.translate(
      'register-verification-email.subject',
      { lang },
    );
    const content = this.i18nService.translate(
      'register-verification-email.content',
      { lang, args: { code: data.code } },
    );
    const ignore = this.i18nService.translate(
      'register-verification-email.ignore',
      { lang },
    );
    const support = this.i18nService.translate(
      'register-verification-email.support',
      { lang },
    );
    const title = this.i18nService.translate(
      'register-verification-email.title',
      { lang },
    );
    const button = this.i18nService.translate(
      'register-verification-email.button',
      { lang },
    );

    await sendMail(
      this.logger,
      this.mailerService,
      'sendNormalRegisterEmail',
      data.to,
      {
        subject,
        to: data.to,
        template: 'register-verification-email',
        context: { title, content, button, ignore, support, link: data.link },
      },
    );
  }
}
