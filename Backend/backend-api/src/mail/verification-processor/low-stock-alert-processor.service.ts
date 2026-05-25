import { Injectable } from '@nestjs/common';
import { MailerService } from '@nestjs-modules/mailer';
import { I18nService } from 'nestjs-i18n';
import { PinoLogger } from 'nestjs-pino';
import type { I18nTranslations } from '#/i18n/generated/i18n.generated.js';
import type {
  LowStockAlertEmailItem,
  LowStockAlertEmailJob,
} from '#/mail/mail.types.js';
import { sendMail } from '#/utils/mailer.utils.js';

@Injectable()
export class LowStockAlertProcessorService {
  constructor(
    private readonly mailerService: MailerService,
    private readonly logger: PinoLogger,
    private readonly i18nService: I18nService<I18nTranslations>,
  ) {}

  async sendLowStockAlert(data: LowStockAlertEmailJob) {
    const lang = data.lang ?? 'en';
    const args = {
      companyName: this.escapeHtml(data.companyName),
      count: String(data.items.length),
    };
    const subject = this.t(lang, 'subject', args);
    const title = this.t(lang, 'title', args);
    const intro = this.t(lang, 'intro', args);
    const content = `${intro}<br><br>${data.items
      .map((item) => this.formatItem(lang, item))
      .join('<br>')}`;
    const ignore = this.t(lang, 'ignore', args);
    const support = this.t(lang, 'support', args);

    await sendMail(
      this.logger,
      this.mailerService,
      'sendLowStockAlert',
      data.to,
      {
        subject,
        to: data.to,
        template: 'activation-with-temp-password',
        context: { title, content, ignore, support },
      },
    );
  }

  private formatItem(lang: string, item: LowStockAlertEmailItem): string {
    return this.t(lang, 'item', {
      productName: this.escapeHtml(item.productName),
      productCode: this.escapeHtml(item.productCode),
      variantProductCode: this.escapeHtml(item.variantProductCode),
      availableStock: String(item.availableStock),
      lowStockThreshold: String(item.lowStockThreshold),
    });
  }

  private t(
    language: string,
    key: string,
    args?: Record<string, string>,
  ): string {
    return this.i18nService.translate(`low-stock-alert.${key}` as never, {
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
}
