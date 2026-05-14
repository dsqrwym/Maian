import { Controller, Req, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { TypedParam, TypedRoute } from '@nestia/core';
import { FastifyRequest } from 'fastify';

import { WriteCartsService } from '#/carts/services/write-carts.service.js';
import { TypedBody } from '#/utils/typia/typed-body.typia.js';
import {
  ICreateCartItemDto,
  validateICreateCartItem,
} from '#/carts/dto/create-cart-item.dto.js';
import { JwtAuthGuard } from '#/auth/guard/auth.guard.js';
import {
  IUpdateCartItem,
  validateIUpdateCartItem,
} from '#/carts/dto/update-cart-item.dto.js';
import { TagsIntegerString } from '#/utils/typia/tags/string.tag.js';
import { TagsUuid } from '#/utils/typia/validators/auth.validator.js';

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

  @TypedRoute.Patch('items/:id')
  @ApiOperation({
    summary: 'Update quantity of a cart item',
  })
  async updateCartItem(
    @TypedBody(validateIUpdateCartItem) dto: IUpdateCartItem,
    @TypedParam('id') cartDetailId: TagsIntegerString,
    @Req() req: FastifyRequest,
  ) {
    return this.writeCartsService.updateQuantity(
      dto,
      cartDetailId,
      req.user.userId,
      req.ability,
    );
  }

  @TypedRoute.Delete('items/:id')
  @ApiOperation({
    summary: 'Delete a cart item',
  })
  async deleteCartItem(
    @TypedParam('id') cartDetailId: TagsIntegerString,
    @Req() req: FastifyRequest,
  ) {
    return this.writeCartsService.deleteCartItem(
      cartDetailId,
      req.user.userId,
      req.ability,
    );
  }

  @TypedRoute.Delete('wholesalers/:id')
  @ApiOperation({
    summary: 'Delete all cart items for a wholesaler',
  })
  async deleteWholesalerCart(
    @TypedParam('id') wholesalerId: TagsUuid,
    @Req() req: FastifyRequest,
  ) {
    return this.writeCartsService.deleteCart(
      wholesalerId,
      req.user.userId,
      req.ability,
    );
  }
}
