import { Controller, Req, UseGuards } from '@nestjs/common';
import { seconds, Throttle } from '@nestjs/throttler';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import type { FastifyRequest } from 'fastify';
import { JwtAuthGuard } from '#/auth/guard/auth.guard.js';
import { TypedQuery, TypedRoute } from '@nestia/core';
import { PaginatedDataWithT } from '#/common/types-interfaces/response.interface.js';
import { ReadWholesalerService } from '#/user/services/read-wholesaler.services.js';
import {
  IFindWholesalerQueryDto,
  validateWholesalerQuery,
} from '#/user/dto/find-wholesaler-query.dto.js';

@ApiTags('User')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Throttle({ default: { limit: 10, ttl: seconds(1) } })
@Controller()
export class ReadWholesalerController {
  constructor(private readonly readWholesalerService: ReadWholesalerService) {}

  @TypedRoute.Get('wholesalers')
  async findUser(
    @TypedQuery(validateWholesalerQuery) query: IFindWholesalerQueryDto,
    @Req() req: FastifyRequest,
  ): Promise<
    PaginatedDataWithT<{
      id: string;
      user_id: string | null;
      profile_image_file_id: bigint | null;
      display_name: string | null | undefined;
      company_name: string;
      company_type: string;
      description: string | null | undefined;
      delivery_available: boolean | null | undefined;
      pickup_available: boolean | null | undefined;
      minimum_order_amount: string | null | undefined;
      delivery_area_description: string | null | undefined;
      city: { name: string; name_local: string; id: number } | null;
      province: { name: string; name_local: string; id: number } | null;
    }>
  > {
    return this.readWholesalerService.findWholesalers(query, req.ability);
  }
}
