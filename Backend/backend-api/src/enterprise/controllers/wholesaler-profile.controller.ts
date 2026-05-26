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
/**
 * Controller for wholesaler profile read and write operations.
 *
 * @class WholesalerProfileController
 */
export class WholesalerProfileController {
  constructor(
    private readonly wholesalerProfileService: WholesalerProfileService,
  ) {}

  /**
   * Update the profile for the authenticated wholesaler.
   *
   * @param {FastifyRequest} req - Request object containing the authenticated wholesaler
   * @param {IUpdateWholesalerProfileDto} body - Wholesaler profile update payload
   * @returns {Promise<void>}
   */
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

  /**
   * Get the profile for the authenticated wholesaler.
   *
   * @param {FastifyRequest} req - Request object containing the authenticated wholesaler
   * @returns {Promise<WholesalerProfileResponseDto>} Wholesaler profile details
   */
  @TypedRoute.Get()
  async getMyProfile(
    @Req() req: FastifyRequest,
  ): Promise<WholesalerProfileResponseDto> {
    return this.wholesalerProfileService.getWholesalerProfile(
      req.user.wholesalerId ?? req.user.userId,
      req.ability,
    );
  }

  /**
   * Get a wholesaler profile by ID.
   *
   * @param {string} id - Wholesaler ID
   * @param {FastifyRequest} req - Request object containing user ability
   * @returns {Promise<WholesalerProfileResponseDto>} Wholesaler profile details
   */
  @TypedRoute.Get(':id')
  async getWholesalerProfileById(
    @TypedParam('id') id: string,
    @Req() req: FastifyRequest,
  ): Promise<WholesalerProfileResponseDto> {
    return this.wholesalerProfileService.getWholesalerProfile(id, req.ability);
  }
}
