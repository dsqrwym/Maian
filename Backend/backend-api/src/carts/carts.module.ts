import { Module } from '@nestjs/common';
import { WriteCartsController } from '#/carts/controllers/write-carts.controller.js';
import { WriteCartsService } from '#/carts/services/write-carts.service.js';
import { DrizzleModule } from '#/drizzle/drizzle.module.js';
import { ReadCartsController } from '#/carts/controllers/read-carts.controller.js';
import { ReadCartsService } from '#/carts/services/read-carts.service.js';

@Module({
  imports: [DrizzleModule],
  controllers: [WriteCartsController, ReadCartsController],
  providers: [WriteCartsService, ReadCartsService],
  exports: [],
})
export class CartsModule {}
