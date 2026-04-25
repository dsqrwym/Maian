import { Injectable } from '@nestjs/common';
import { MailerService } from '@nestjs-modules/mailer';
import { PinoLogger } from 'nestjs-pino';
import { I18nService } from 'nestjs-i18n';
import { ResetPasswordJob } from '../mail.types';
import { I18nTranslations } from '@/i18n/generated/i18n.generated';
import { sendMail } from '@/utils/mailer.utils';

@Injectable()
export class VerifyResetPasswordProcessorService {
  constructor(
    private readonly mailerService: MailerService,
    private readonly logger: PinoLogger,
    private readonly i18nService: I18nService<I18nTranslations>,
  ) {}
  async sendResetPassword(data: ResetPasswordJob) {
    const { name, to, code } = data;
    const lang = data.lang || 'en';
    if (!to || !name) {
      this.logger.error('sendResetPassword missing user email or name');
      return;
    }
    const namespace = 'reset-password.';
    const subject: string = this.i18nService.translate(
      `${namespace}resetPasswordSubject`,
      { lang },
    );
    const greeting: string = this.i18nService.translate(
      `${namespace}greeting`,
      {
        lang,
        args: { username: name },
      },
    );
    const resetPasswordMessage: string = this.i18nService.translate(
      `${namespace}resetPasswordMessage`,
      {
        lang,
        args: { code },
      },
    );
    const ignoreMessage: string = this.i18nService.translate(
      `${namespace}ignoreMessage`,
      { lang },
    );

    await sendMail(this.logger, this.mailerService, 'sendResetPassword', to, {
      to,
      subject,
      template: 'reset-password',
      context: { greeting, resetPasswordMessage, ignoreMessage },
    });
  }
}
