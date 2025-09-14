import { Processor, WorkerHost } from '@nestjs/bullmq';
import { Job } from 'bullmq';
import { Injectable } from '@nestjs/common';
import { MailerService } from '@nestjs-modules/mailer';
import { PinoLogger } from 'nestjs-pino';
import { I18nService } from 'nestjs-i18n';
import { I18nTranslations } from '../i18n/generated/i18n.generated';
import { maskEmail } from '../common/formatter/emial-format';

type VerificationEmailJob = {
  to: string;
  lang?: string;
  link: string;
};

type ResetPasswordJob = {
  user: { email: string; name: string; language?: string };
  code: string;
};

@Processor('mail')
@Injectable()
export class MailQueueProcessor extends WorkerHost {
  constructor(
    private readonly mailerService: MailerService,
    private readonly logger: PinoLogger,
    private readonly i18nService: I18nService<I18nTranslations>,
  ) {
    super();
  }

  async process(job: Job): Promise<any> {
    switch (job.name) {
      case 'sendVerificationEmail': {
        return this.sendVerification(job.data as VerificationEmailJob);
      }
      case 'sendResetPassword': {
        return this.sendResetPassword(job.data as ResetPasswordJob);
      }
      default: {
        this.logger.warn(`Unknown mail job: ${job.name}`);
        return;
      }
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
