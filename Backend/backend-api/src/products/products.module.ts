import { Module } from '@nestjs/common';
import { ProductsService } from './products.service.js';
import { ProductsController } from './products.controller.js';
import { ProductsWriteService } from '#/products/services/products-write.service.js';
import { ProductsReadService } from '#/products/services/products-read.service.js';

@Module({
  controllers: [ProductsController],
  providers: [ProductsService, ProductsReadService, ProductsWriteService],
})
export class ProductsModule {}
