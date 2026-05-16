import { Controller, Req, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '#/auth/guard/auth.guard.js';
import { ReadOrderService } from '#/orders/services/read-order.service.js';
import { RolesGuard } from '#/common/guards/roles.guard.js';
import { TypedQuery, TypedRoute } from '@nestia/core';
import {
  IOrderQuery,
  validateOrderQuery,
} from '#/orders/dto/order-query.dto.js';
import { FastifyRequest } from 'fastify';
import { RolesAllowed } from '#/common/guards/decorator/roles-allowed.decorator.js';
import { UserRole } from '#/generated/drizzle/enums.js';
import { IOrderResponse } from '#/orders/dto/order-response.dto.js';
import { PaginatedDataWithT } from '#/common/types-interfaces/response.interface.js';
import { WHOLESALER_ROLES } from '#/enterprise/enterprise.constants.js';

@ApiTags('Orders')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard, RolesGuard)
@Controller()
export class ReadOrderController {
  constructor(private readonly readOrderService: ReadOrderService) {}

  @RolesAllowed(UserRole.RETAILER)
  @TypedRoute.Get('retailer')
  async getMyOrdersByRetailer(
    @TypedQuery(validateOrderQuery) query: IOrderQuery,
    @Req() req: FastifyRequest,
  ): Promise<PaginatedDataWithT<IOrderResponse>> {
    return this.readOrderService.getMyOrders(
      query,
      req.ability,
      req.user.userId,
    );
  }

  @RolesAllowed(...WHOLESALER_ROLES)
  @TypedRoute.Get('wholesaler')
  async getMyOrdersByWholesaler(
    @TypedQuery(validateOrderQuery) query: IOrderQuery,
    @Req() req: FastifyRequest,
  ): Promise<PaginatedDataWithT<IOrderResponse>> {
    return this.readOrderService.getMyOrders(
      query,
      req.ability,
      undefined,
      req.user.userId,
    );
  }
}
