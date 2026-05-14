import { Controller, Req, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { TypedQuery, TypedRoute } from '@nestia/core';
import { FastifyRequest } from 'fastify';
import { JwtAuthGuard } from '#/auth/guard/auth.guard.js';
import {
  ICartsQueryDto,
  validateICartsQueryDto,
} from '#/carts/dto/carts-query.dto.js';
import { ReadCartsService } from '#/carts/services/read-carts.service.js';
import { ICartResponse } from '../dto/carts-response.dto.js';
import { RolesGuard } from '#/common/guards/roles.guard.js';
import { RolesAllowed } from '#/common/guards/decorator/roles-allowed.decorator.js';
import { UserRole } from '#/generated/drizzle/enums.js';

@ApiTags('Carts')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard, RolesGuard)
@RolesAllowed(UserRole.RETAILER)
@Controller()
export class ReadCartsController {
  constructor(private readonly readCartsService: ReadCartsService) {}

  @TypedRoute.Get()
  async getMyCartInfo(
    @TypedQuery(validateICartsQueryDto) query: ICartsQueryDto,
    @Req() req: FastifyRequest,
  ): Promise<ICartResponse> {
    return this.readCartsService.getMyCartInfo(query, req.user);
  }
}
