import { Module } from '@nestjs/common';
import { WriteCartsController } from '#/carts/controllers/write-carts.controller.js';
import { WriteCartsService } from '#/carts/services/write-carts.service.js';
import { DrizzleModule } from '#/drizzle/drizzle.module.js';

@Module({
  imports: [DrizzleModule],
  controllers: [WriteCartsController],
  providers: [WriteCartsService],
  exports: [],
})
export class CartsModule {}
