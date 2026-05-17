import { Module } from '@nestjs/common';
import { DrizzleModule } from '#/drizzle/drizzle.module.js';
import { WriteOrderController } from '#/orders/controllers/write-order.controller.js';
import { WriteOrderService } from '#/orders/services/write-order.service.js';
import { ReadOrderController } from './controllers/read-order.controller.js';
import { ReadOrderService } from '#/orders/services/read-order.service.js';
import { FilterOrderMetadataController } from '#/orders/controllers/filter-order-metadata.controller.js';
import { FilterOrderMetadataService } from '#/orders/services/filter-order-metadata.service.js';
import { PDFModule } from '#/pdf/pdf.module.js';
import { FilesModule } from '#/files/files.module.js';
import { MailModule } from '#/mail/mail.module.js';
import { OrderPdfNotificationService } from '#/orders/services/order-pdf-notification.service.js';

@Module({
  imports: [DrizzleModule, PDFModule, FilesModule, MailModule],
  controllers: [
    WriteOrderController,
    ReadOrderController,
    FilterOrderMetadataController,
  ],
  providers: [
    WriteOrderService,
    ReadOrderService,
    FilterOrderMetadataService,
    OrderPdfNotificationService,
  ],
})
export class OrderModule {}
