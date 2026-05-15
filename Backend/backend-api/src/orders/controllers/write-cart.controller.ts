import { Controller, Req, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '#/auth/guard/auth.guard.js';
import { WriteOrderService } from '#/orders/services/write-order.service.js';
import { TypedBody } from '#/utils/typia/typed-body.typia.js';
import {
  ICreateOrderDto,
  validateCreateOrder,
} from '#/orders/dto/create-order.dto.js';
import { FastifyRequest } from 'fastify';
import { TypedRoute } from '@nestia/core';

@ApiTags('Orders')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller()
export class WriteOrderController {
  constructor(private readonly writeOrderService: WriteOrderService) {}

  @TypedRoute.Post('from-cart')
  @ApiOperation({
    summary: 'Create a pending order from the current retailer cart',
  })
  async createFromCart(
    @TypedBody(validateCreateOrder) dto: ICreateOrderDto,
    @Req() req: FastifyRequest,
  ) {
    return this.writeOrderService.createFromCart(
      req.user.userId,
      dto,
      req.ability,
    );
  }
}
