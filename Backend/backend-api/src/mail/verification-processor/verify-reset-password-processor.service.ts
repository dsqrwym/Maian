import { Injectable } from '@nestjs/common';
import { MailerService } from '@nestjs-modules/mailer';
import { PinoLogger } from 'nestjs-pino';
import { I18nTranslations } from '../../generated/i18n.generated';
import { I18nService } from 'nestjs-i18n';
import { ResetPasswordJob } from '../mail.types';
import { maskEmail } from '../../common/formatter/emial-format';

@Injectable()
export class VerifyResetPasswordProcessorService {
  constructor(
    private readonly mailerService: MailerService,
    private readonly logger: PinoLogger,
    private readonly i18nService: I18nService<I18nTranslations>,
  ) {}
  async sendResetPassword(data: ResetPasswordJob) {
    const { user, code } = data;
    if (!user?.email || !user?.name) {
      this.logger.error('sendResetPassword missing user email or name');
      return;
    }
    const lang = user.language || 'en';
    const namespace = 'reset-password.';
    const subject: string = this.i18nService.translate(
      `${namespace}resetPasswordSubject`,
      { lang },
    );
    const greeting: string = this.i18nService.translate(
      `${namespace}greeting`,
      {
        lang,
        args: { username: user.name },
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

    try {
      const info: unknown = await this.mailerService.sendMail({
        to: user.email,
        subject,
        template: 'reset-password',
        context: { greeting, resetPasswordMessage, ignoreMessage },
      });
      this.logger.info(
        `Email sent successfully to ${maskEmail(user.email)} with info: ${JSON.stringify(info)}`,
      );
      return info;
    } catch (err) {
      const error = err instanceof Error ? err : new Error(String(err));
      this.logger.error(
        `sendResetPassword failed: ${error.message}`,
        error.stack,
      );
      throw error; // BullMQ 能正确识别并 retry
    }
  }
}
