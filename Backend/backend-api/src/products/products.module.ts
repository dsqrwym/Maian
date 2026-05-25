import { Module } from '@nestjs/common';
import { ProductsService } from './products.service.js';
import { ProductsController } from './products.controller.js';
import { WriteProductsService } from './services/write-products.service.js';
import { ReadProductsService } from './services/read-products.service.js';
import { MailModule } from '#/mail/mail.module.js';

@Module({
  imports: [MailModule],
  controllers: [ProductsController],
  providers: [ProductsService, ReadProductsService, WriteProductsService],
  exports: [ReadProductsService],
})
export class ProductsModule {}
