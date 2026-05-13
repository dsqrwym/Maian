import { Controller, Req, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { TypedRoute } from '@nestia/core';
import { FastifyRequest } from 'fastify';

import { WriteCartsService } from '#/carts/services/write-carts.service.js';
import { TypedBody } from '#/utils/typia/typed-body.typia.js';
import {
  ICreateCartItemDto,
  validateICreateCartItem,
} from '#/carts/dto/create-cart-item.dto.js';
import { JwtAuthGuard } from '#/auth/guard/auth.guard.js';

@ApiTags('Carts')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller()
export class WriteCartsController {
  constructor(private readonly writeCartsService: WriteCartsService) {}

  @TypedRoute.Post('items')
  @ApiOperation({
    summary: 'Add an item to the current retailer cart',
  })
  async addCartItem(
    @TypedBody(validateICreateCartItem) dto: ICreateCartItemDto,
    @Req() req: FastifyRequest,
  ) {
    return this.writeCartsService.addCartItem(
      dto,
      req.user.userId,
      req.ability,
    );
  }
}
