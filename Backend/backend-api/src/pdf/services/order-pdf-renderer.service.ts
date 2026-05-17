import { Injectable } from '@nestjs/common';
import type { Readable } from 'node:stream';
import pdfMake from 'pdfmake';
import {
  ORDER_PDF_ALLOWED_FONT_PATHS,
  ORDER_PDF_FONTS,
} from '#/pdf/pdf-font.constants.js';
import type {
  IOrderPdfData,
  IOrderPdfLabels,
  PdfMakeWithPolicies,
} from '#/pdf/pdf.type.js';
import { buildOrderPdfTemplate } from '#/pdf/templates/order-pdf.template.js';

@Injectable()
export class OrderPdfRendererService {
  private static pdfMakeConfigured = false;

  async renderOrderPdf(
    data: IOrderPdfData,
    labels: IOrderPdfLabels,
  ): Promise<Buffer> {
    this.configurePdfMake();
    const docDefinition = buildOrderPdfTemplate(data, labels);
    return await pdfMake.createPdf(docDefinition).getBuffer();
  }

  private configurePdfMake() {
    if (OrderPdfRendererService.pdfMakeConfigured) return;

    pdfMake.setFonts(ORDER_PDF_FONTS);

    const pdfMakeWithPolicies = pdfMake as PdfMakeWithPolicies;
    pdfMakeWithPolicies.setLocalAccessPolicy?.((filePath) =>
      ORDER_PDF_ALLOWED_FONT_PATHS.has(filePath),
    );
    pdfMakeWithPolicies.setUrlAccessPolicy?.(() => false);

    OrderPdfRendererService.pdfMakeConfigured = true;
  }
}
