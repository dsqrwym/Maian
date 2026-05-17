import { Module } from '@nestjs/common';
import { DrizzleModule } from '#/drizzle/drizzle.module.js';
import { MyI18nModule } from '#/i18n/i18n.module.js';
import { OrderPdfService } from '#/pdf/services/order-pdf.service.js';
import { OrderPdfDataService } from '#/pdf/services/order-pdf-data.service.js';
import { OrderPdfLabelsService } from '#/pdf/services/order-pdf-labels.service.js';
import { OrderPdfRendererService } from '#/pdf/services/order-pdf-renderer.service.js';

@Module({
  imports: [DrizzleModule, MyI18nModule],
  providers: [
    OrderPdfService,
    OrderPdfDataService,
    OrderPdfLabelsService,
    OrderPdfRendererService,
  ],
  exports: [OrderPdfService],
})
export class PDFModule {}
