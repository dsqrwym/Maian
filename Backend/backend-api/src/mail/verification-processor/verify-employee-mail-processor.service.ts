import { Injectable } from '@nestjs/common';
import { MailerService } from '@nestjs-modules/mailer';
import { PinoLogger } from 'nestjs-pino';
import { I18nService } from 'nestjs-i18n';
import { I18nTranslations } from '@/i18n/generated/i18n.generated';
import {
  ActiveEmployeeWithPasswordEmailJob,
  VerifyEmployeeEmailJob,
} from '../mail.types';
import { sendMail } from '@/utils/mailer.utils';

@Injectable()
export class VerifyEmployeeMailProcessorService {
  constructor(
    private readonly mailerService: MailerService,
    private readonly logger: PinoLogger,
    private readonly i18nService: I18nService<I18nTranslations>,
  ) {}

  async sendVerifyEmployeeEmail(data: VerifyEmployeeEmailJob) {
    const lang = data.lang || 'en';
    const subject = this.i18nService.translate(
      'verify-employee-email.subject',
      { lang },
    );
    const content1 = this.i18nService.translate(
      'verify-employee-email.content1',
      {
        lang,
        args: { companyName: data.companyName, position: data.position },
      },
    );
    const content2 = this.i18nService.translate(
      'verify-employee-email.content2',
      { lang },
    );
    const ignore = this.i18nService.translate('verify-employee-email.ignore', {
      lang,
      args: { companyName: data.companyName },
    });
    const support = this.i18nService.translate(
      'verify-employee-email.support',
      { lang },
    );
    const title = this.i18nService.translate('verify-employee-email.title', {
      lang,
    });
    const button = this.i18nService.translate('verify-employee-email.button', {
      lang,
    });

    await sendMail(
      this.logger,
      this.mailerService,
      'sendVerifyEmployeeEmail',
      data.to,
      {
        subject,
        to: data.to,
        template: 'verification-email',
        context: {
          title,
          content1,
          content2,
          button,
          ignore,
          support,
          link: data.link,
        },
      },
    );
  }

  async sendActiveEmployeeWithPasswordEmail(
    data: ActiveEmployeeWithPasswordEmailJob,
  ) {
    const lang = data.lang || 'en';
    const subject = this.i18nService.translate('employee-activation.subject', {
      lang,
    });
    const content = this.i18nService.translate('employee-activation.content', {
      lang,
      args: {
        companyName: data.companyName,
        employeeName: data.employeeName,
        email: data.to,
        temporaryPassword: data.temporaryPassword,
      },
    });
    const ignore = this.i18nService.translate('employee-activation.ignore', {
      lang,
    });
    const support = this.i18nService.translate('employee-activation.support', {
      lang,
    });
    const title = this.i18nService.translate('employee-activation.title', {
      lang,
    });
    await sendMail(
      this.logger,
      this.mailerService,
      'sendActiveEmployeeWithPasswordEmail',
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
