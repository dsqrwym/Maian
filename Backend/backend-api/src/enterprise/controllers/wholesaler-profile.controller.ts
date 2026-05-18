import { Controller, Req, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { seconds, Throttle } from '@nestjs/throttler';
import { WholesalerProfileService } from '#/enterprise/services/wholesaler-profile.service.js';
import { TypedParam, TypedRoute } from '@nestia/core';
import { FastifyRequest } from 'fastify';
import {
  IUpdateWholesalerProfileDto,
  validateUpdateWholesalerProfile,
} from '#/enterprise/dto/update-wholesaler-profile.dto.js';
import { TypedBody } from '#/utils/typia/typed-body.typia.js';
import { JwtAuthGuard } from '#/auth/guard/auth.guard.js';
import { WholesalerProfileResponseDto } from '#/enterprise/dto/wholesaler-profile-response.dto.js';

@ApiTags('Employee Management')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Throttle({ default: { limit: 2, ttl: seconds(1) } })
@Controller('wholesaler-profile')
export class WholesalerProfileController {
  constructor(
    private readonly wholesalerProfileService: WholesalerProfileService,
  ) {}

  @TypedRoute.Patch()
  async updateWholesalerProfile(
    @Req() req: FastifyRequest,
    @TypedBody(validateUpdateWholesalerProfile)
    body: IUpdateWholesalerProfileDto,
  ): Promise<void> {
    return this.wholesalerProfileService.updateWholesalerProfile(
      req.user.userId,
      body,
      req.ability,
    );
  }

  @TypedRoute.Get()
  async getMyProfile(
    @Req() req: FastifyRequest,
  ): Promise<WholesalerProfileResponseDto> {
    return this.wholesalerProfileService.getWholesalerProfile(
      req.user.wholesalerId ?? req.user.userId,
      req.ability,
    );
  }

  @TypedRoute.Get(':id')
  async getWholesalerProfileById(
    @TypedParam('id') id: string,
    @Req() req: FastifyRequest,
  ): Promise<WholesalerProfileResponseDto> {
    return this.wholesalerProfileService.getWholesalerProfile(id, req.ability);
  }
}
