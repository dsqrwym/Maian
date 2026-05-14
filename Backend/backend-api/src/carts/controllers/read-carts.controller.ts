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

@ApiTags('Carts')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller()
export class ReadCartsController {
  constructor(private readonly readCartsService: ReadCartsService) {}

  @TypedRoute.Get()
  async getMyCartInfo(
    @TypedQuery(validateICartsQueryDto) query: ICartsQueryDto,
    @Req() req: FastifyRequest,
  ): Promise<ICartResponse> {
    return this.readCartsService.getMyCartInfo(query, req.user, req.ability);
  }
}
