import { Module } from '@nestjs/common';
import { DrizzleModule } from '#/drizzle/drizzle.module.js';
import { WriteOrderController } from '#/orders/controllers/write-cart.controller.js';
import { WriteOrderService } from '#/orders/services/write-order.service.js';

@Module({
  imports: [DrizzleModule],
  controllers: [WriteOrderController],
  providers: [WriteOrderService],
})
export class OrderModule {}
