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
import { TypedParam, TypedRoute } from '@nestia/core';
import { TagsIntegerString } from '#/utils/typia/tags/string.tag.js';
import {
  ICancelOrderDto,
  IRejectOrderDto,
  validateICancelOrderDto,
  validateIRejectOrderDto,
} from '#/orders/dto/change-order-status.dto.js';
import {
  IUpdateOrderDto,
  validateIUpdateOrderDto,
} from '#/orders/dto/update-order.dto.js';

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

  @TypedRoute.Post(':id/cancel')
  @ApiOperation({
    summary: 'Cancel a pending order by the current retailer',
  })
  async cancelByRetailer(
    @TypedParam('id') id: TagsIntegerString,
    @TypedBody(validateICancelOrderDto) dto: ICancelOrderDto,
    @Req() req: FastifyRequest,
  ) {
    return this.writeOrderService.cancelOrderByRetailer(
      id,
      req.user.userId,
      dto,
      req.ability,
    );
  }

  @TypedRoute.Post(':id/reject')
  @ApiOperation({
    summary: 'Reject a pending order by the current wholesaler',
  })
  async rejectByWholesaler(
    @TypedParam('id') id: TagsIntegerString,
    @TypedBody(validateIRejectOrderDto) dto: IRejectOrderDto,
    @Req() req: FastifyRequest,
  ) {
    return this.writeOrderService.rejectByWholesaler(
      id,
      req.user.wholesalerId ?? req.user.userId,
      req.user.userId,
      dto,
      req.ability,
    );
  }

  @TypedRoute.Post(':id/accept')
  @ApiOperation({
    summary: 'Accept a pending order by the current wholesaler',
  })
  async acceptByWholesaler(
    @TypedParam('id') id: TagsIntegerString,
    @Req() req: FastifyRequest,
  ) {
    return this.writeOrderService.acceptOrderByWholesaler(
      id,
      req.user.wholesalerId ?? req.user.userId,
      req.user.userId,
      req.ability,
    );
  }

  @TypedRoute.Patch(':id/estimated-delivery-date')
  async updateDeliveryDate(
    @TypedParam('id') id: TagsIntegerString,
    @TypedBody(validateIUpdateOrderDto) dto: IUpdateOrderDto,
    @Req() req: FastifyRequest,
  ) {
    return this.writeOrderService.updateDeliveryDate(
      id,
      req.user.wholesalerId ?? req.user.userId,
      dto,
      req.ability,
    );
  }
}
