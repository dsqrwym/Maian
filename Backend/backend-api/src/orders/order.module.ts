import { Module } from '@nestjs/common';
import { DrizzleModule } from '#/drizzle/drizzle.module.js';
import { WriteOrderController } from '#/orders/controllers/write-order.controller.js';
import { WriteOrderService } from '#/orders/services/write-order.service.js';
import { ReadOrderController } from './controllers/read-order.controller.js';
import { ReadOrderService } from '#/orders/services/read-order.service.js';
import { FilterOrderMetadataController } from '#/orders/controllers/filter-order-metadata.controller.js';
import { FilterOrderMetadataService } from '#/orders/services/filter-order-metadata.service.js';

@Module({
  imports: [DrizzleModule],
  controllers: [
    WriteOrderController,
    ReadOrderController,
    FilterOrderMetadataController,
  ],
  providers: [WriteOrderService, ReadOrderService, FilterOrderMetadataService],
})
export class OrderModule {}
