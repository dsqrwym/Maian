import { Injectable } from '@nestjs/common';
import { MailerService } from '@nestjs-modules/mailer';
import { I18nService } from 'nestjs-i18n';
import { PinoLogger } from 'nestjs-pino';
import type { I18nTranslations } from '#/i18n/generated/i18n.generated.js';
import { FilesService } from '#/files/files.service.js';
import type { OrderPdfNotificationEmailJob } from '../mail.types.js';
import { sendMail } from '#/utils/mailer.utils.js';

@Injectable()
export class OrderPdfNotificationProcessorService {
  constructor(
    private readonly mailerService: MailerService,
    private readonly logger: PinoLogger,
    private readonly i18nService: I18nService<I18nTranslations>,
    private readonly filesService: FilesService,
  ) {}

  async sendOrderPdfNotification(data: OrderPdfNotificationEmailJob) {
    const lang = data.lang ?? 'en';
    const file = await this.filesService.getFileById(data.fileId);
    const args = {
      orderNumber: this.escapeHtml(data.orderNumber),
      recipientName: this.escapeHtml(data.recipientName ?? ''),
      counterpartyName: this.escapeHtml(data.counterpartyName ?? ''),
      reason: this.formatReason(
        data.actionReason ?? this.t(lang, 'mail.noReason'),
      ),
    };
    const subject = this.t(lang, `mail.${data.type}.subject`, args);
    const title = this.t(lang, `mail.${data.type}.title`, args);
    const content = this.t(lang, `mail.${data.type}.content`, args);
    const ignore = this.t(lang, 'mail.ignore', args);
    const support = this.t(lang, 'mail.support', args);

    await sendMail(
      this.logger,
      this.mailerService,
      'sendOrderPdfNotification',
      data.to,
      {
        subject,
        to: data.to,
        template: 'activation-with-temp-password',
        context: { title, content, ignore, support },
        attachments: [
          {
            filename: file.filename,
            content: file.stream,
            contentType: file.mime_type,
          },
        ],
      },
    );
  }

  private t(
    language: string,
    key: string,
    args?: Record<string, string>,
  ): string {
    return this.i18nService.translate(`order-pdf.${key}` as never, {
      lang: language,
      args,
    });
  }

  private escapeHtml(value: string): string {
    return value
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  private formatReason(value: string): string {
    return this.escapeHtml(value).replace(/\r?\n/g, '<br>');
  }
}
