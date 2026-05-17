import { Injectable } from '@nestjs/common';
import { I18nService } from 'nestjs-i18n';
import type { I18nTranslations } from '#/i18n/generated/i18n.generated.js';
import type { IOrderPdfLabels } from '#/pdf/pdf.type.js';

@Injectable()
export class OrderPdfLabelsService {
  constructor(private readonly i18nService: I18nService<I18nTranslations>) {}

  getPdfLabels(language: string): IOrderPdfLabels {
    return {
      documentTitle: this.i18nService.translate(`order-pdf.documentTitle`, {
        lang: language,
      }),
      order: this.i18nService.translate('order-pdf.order', { lang: language }),
      page: this.i18nService.translate('order-pdf.page', { lang: language }),
      series: this.i18nService.translate('order-pdf.series', {
        lang: language,
      }),
      year: this.i18nService.translate('order-pdf.year', { lang: language }),
      sequence: this.i18nService.translate('order-pdf.sequence', {
        lang: language,
      }),
      date: this.i18nService.translate('order-pdf.date', { lang: language }),
      seller: this.i18nService.translate('order-pdf.seller', {
        lang: language,
      }),
      buyer: this.i18nService.translate('order-pdf.buyer', { lang: language }),
      companyType: this.i18nService.translate('order-pdf.companyType', {
        lang: language,
      }),
      taxId: this.i18nService.translate('order-pdf.taxId', { lang: language }),
      contact: this.i18nService.translate('order-pdf.contact', {
        lang: language,
      }),
      email: this.i18nService.translate('order-pdf.email', { lang: language }),
      phone: this.i18nService.translate('order-pdf.phone', { lang: language }),
      shippingAddress: this.i18nService.translate('order-pdf.shippingAddress', {
        lang: language,
      }),
      country: this.i18nService.translate('order-pdf.country', {
        lang: language,
      }),
      documentSummary: this.i18nService.translate('order-pdf.documentSummary', {
        lang: language,
      }),
      number: this.i18nService.translate('order-pdf.number', {
        lang: language,
      }),
      currency: this.i18nService.translate('order-pdf.currency', {
        lang: language,
      }),
      lineCount: this.i18nService.translate('order-pdf.lineCount', {
        lang: language,
      }),
      itemsTitle: this.i18nService.translate('order-pdf.itemsTitle', {
        lang: language,
      }),
      product: this.i18nService.translate('order-pdf.product', {
        lang: language,
      }),
      code: this.i18nService.translate('order-pdf.code', { lang: language }),
      saleUnit: this.i18nService.translate('order-pdf.saleUnit', {
        lang: language,
      }),
      quantity: this.i18nService.translate('order-pdf.quantity', {
        lang: language,
      }),
      price: this.i18nService.translate('order-pdf.price', { lang: language }),
      ivaPercent: this.i18nService.translate('order-pdf.ivaPercent', {
        lang: language,
      }),
      subtotal: this.i18nService.translate('order-pdf.subtotal', {
        lang: language,
      }),
      iva: this.i18nService.translate('order-pdf.iva', { lang: language }),
      total: this.i18nService.translate('order-pdf.total', { lang: language }),
      variant: this.i18nService.translate('order-pdf.variant', {
        lang: language,
      }),
      taxableBase: this.i18nService.translate('order-pdf.taxableBase', {
        lang: language,
      }),
      discount: this.i18nService.translate('order-pdf.discount', {
        lang: language,
      }),
      grandTotal: this.i18nService.translate('order-pdf.grandTotal', {
        lang: language,
      }),
    };
  }
}
