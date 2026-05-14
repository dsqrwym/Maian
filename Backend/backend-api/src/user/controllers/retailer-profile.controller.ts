import { Controller, Req, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { TypedParam, TypedRoute } from '@nestia/core';
import { seconds, Throttle } from '@nestjs/throttler';
import type { FastifyRequest } from 'fastify';

import { JwtAuthGuard } from '#/auth/guard/auth.guard.js';
import { TypedBody } from '#/utils/typia/typed-body.typia.js';
import type { RetailerProfileResponseDto } from '#/user/dto/retailer-profile-response.dto.js';
import {
  IUpdateRetailerProfileDto,
  validateUpdateRetailerProfile,
} from '#/user/dto/update-retailer-profile.dto.js';
import { RetailerProfileService } from '#/user/services/retailer-profile.service.js';

@ApiTags('User')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Throttle({ default: { limit: 2, ttl: seconds(1) } })
@Controller('retailer-profile')
export class RetailerProfileController {
  constructor(
    private readonly retailerProfileService: RetailerProfileService,
  ) {}

  @TypedRoute.Patch()
  async updateRetailerProfile(
    @Req() req: FastifyRequest,
    @TypedBody(validateUpdateRetailerProfile)
    body: IUpdateRetailerProfileDto,
  ): Promise<void> {
    return this.retailerProfileService.updateRetailerProfile(
      req.user.userId,
      body,
      req.ability,
    );
  }

  @TypedRoute.Get()
  async getMyProfile(
    @Req() req: FastifyRequest,
  ): Promise<RetailerProfileResponseDto> {
    return this.retailerProfileService.getRetailerProfile(
      req.user.userId,
      req.ability,
    );
  }

  @TypedRoute.Get(':id')
  async getRetailerProfileById(
    @TypedParam('id') id: string,
    @Req() req: FastifyRequest,
  ): Promise<RetailerProfileResponseDto> {
    return this.retailerProfileService.getRetailerProfile(id, req.ability);
  }
}
