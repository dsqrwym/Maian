import { MailerService } from '@nestjs-modules/mailer';
import { PinoLogger } from 'nestjs-pino';
import { Injectable } from '@nestjs/common';
import { I18nService } from 'nestjs-i18n';
import {
  ActiveAdminWithPasswordEmailJob,
  BaseEmailJobWithLink,
} from '../mail.types.js';
import { I18nTranslations } from '#/i18n/generated/i18n.generated.js';
import { sendMail } from '#/utils/mailer.utils.js';

@Injectable()
export class VerifyAdminMailProcessorService {
  constructor(
    private readonly logger: PinoLogger,
    private readonly mailerService: MailerService,
    private readonly i18nService: I18nService<I18nTranslations>,
  ) {}

  async sendVerifyAdminEmail(data: BaseEmailJobWithLink) {
    const lang = data.lang || 'en';
    const link = data.link;
    const subject = this.i18nService.translate('verify-admin-email.subject', {
      lang,
    });
    const content1 = this.i18nService.translate('verify-admin-email.content1', {
      lang,
    });
    const content2 = this.i18nService.translate('verify-admin-email.content2', {
      lang,
    });
    const ignore = this.i18nService.translate('verify-admin-email.ignore', {
      lang,
    });
    const support = this.i18nService.translate('verify-admin-email.support', {
      lang,
    });
    const title = this.i18nService.translate('verify-admin-email.title', {
      lang,
    });
    const button = this.i18nService.translate('verify-admin-email.button', {
      lang,
    });
    await sendMail(
      this.logger,
      this.mailerService,
      'sendVerifyAdminEmail',
      data.to,
      {
        subject,
        to: data.to,
        template: 'verification-email',
        context: { title, content1, content2, button, ignore, support, link },
      },
    );
  }

  async sendActiveAdminWithPasswordEmail(
    data: ActiveAdminWithPasswordEmailJob,
  ) {
    const lang = data.lang || 'en';
    const subject = this.i18nService.translate('admin-activation.subject', {
      lang,
    });
    const content = this.i18nService.translate('admin-activation.content', {
      lang,
      args: {
        adminName: data.adminName,
        email: data.to,
        temporaryPassword: data.temporaryPassword,
      },
    });
    const ignore = this.i18nService.translate('admin-activation.ignore', {
      lang,
    });
    const support = this.i18nService.translate('admin-activation.support', {
      lang,
    });
    const title = this.i18nService.translate('admin-activation.title', {
      lang,
    });

    await sendMail(
      this.logger,
      this.mailerService,
      'sendActiveAdminWithPasswordEmail',
      data.to,
      {
        subject,
        to: data.to,
        template: 'activation-with-temp-password',
        context: { title, content, ignore, support },
      },
    );
  }
}
