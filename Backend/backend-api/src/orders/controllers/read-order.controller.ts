import { Controller, Req, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '#/auth/guard/auth.guard.js';
import { ReadOrderService } from '#/orders/services/read-order.service.js';
import { RolesGuard } from '#/common/guards/roles.guard.js';
import { TypedParam, TypedQuery, TypedRoute } from '@nestia/core';
import {
  IOrderDetailQuery,
  IOrderQuery,
  validateOrderDetailQuery,
  validateOrderQuery,
} from '#/orders/dto/order-query.dto.js';
import { FastifyRequest } from 'fastify';
import { RolesAllowed } from '#/common/guards/decorator/roles-allowed.decorator.js';
import { UserRole } from '#/generated/drizzle/enums.js';
import {
  IOrderDetailResponse,
  IOrderResponse,
} from '#/orders/dto/order-response.dto.js';
import { PaginatedDataWithT } from '#/common/types-interfaces/response.interface.js';
import { WHOLESALER_ROLES } from '#/enterprise/enterprise.constants.js';

@ApiTags('Orders')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard, RolesGuard)
@Controller()
export class ReadOrderController {
  constructor(private readonly readOrderService: ReadOrderService) {}

  @RolesAllowed(UserRole.RETAILER)
  @TypedRoute.Get('standard')
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
  @TypedRoute.Get('enterprise')
  async getMyOrdersByWholesaler(
    @TypedQuery(validateOrderQuery) query: IOrderQuery,
    @Req() req: FastifyRequest,
  ): Promise<PaginatedDataWithT<IOrderResponse>> {
    return this.readOrderService.getMyOrders(
      query,
      req.ability,
      undefined,
      req.user.wholesalerId ?? req.user.userId,
    );
  }

  @RolesAllowed(UserRole.RETAILER)
  @TypedRoute.Get('standard/:id')
  async getMyOrderDetailByRetailer(
    @TypedParam('id') id: string,
    @TypedQuery(validateOrderDetailQuery) query: IOrderDetailQuery,
    @Req() req: FastifyRequest,
  ): Promise<IOrderDetailResponse> {
    return this.readOrderService.getOrderDetail(
      id,
      query,
      req.ability,
      req.user.userId,
    );
  }

  @RolesAllowed(...WHOLESALER_ROLES)
  @TypedRoute.Get('enterprise/:id')
  async getMyOrderDetailByWholesaler(
    @TypedParam('id') id: string,
    @TypedQuery(validateOrderDetailQuery) query: IOrderDetailQuery,
    @Req() req: FastifyRequest,
  ): Promise<IOrderDetailResponse> {
    return this.readOrderService.getOrderDetail(
      id,
      query,
      req.ability,
      undefined,
      req.user.wholesalerId ?? req.user.userId,
    );
  }
}
