import { Controller, Req, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '#/auth/guard/auth.guard.js';
import { seconds, Throttle } from '@nestjs/throttler';
import { RolesAllowed } from '#/common/guards/decorator/roles-allowed.decorator.js';
import { UserRole } from '#/generated/drizzle/enums.js';
import { WholesalerProfileService } from '../services/wholesaler-profile.service.js';
import { TypedRoute } from '@nestia/core';
import { FastifyRequest } from 'fastify';
import { TypedBody } from '#/utils/typia/typed-body.typia.js';
import {
  IUpdateWholesalerProfileDto,
  validateUpdateWholesalerProfile,
} from '#/user/dto/update-wholesaler-profile.dto.js';
import { IWholesalerProfile } from '#/enterprise/types/IWholesalerProfile.js';

@ApiTags('User')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Throttle({ default: { limit: 2, ttl: seconds(1) } })
@RolesAllowed(UserRole.WHOLESALER)
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
  async getMyProfile(@Req() req: FastifyRequest): Promise<{
    email: string;
    user_id: string | null;
    first_name: string | null;
    last_name: string | null;
    username: string | null;
    telephone: string | null;
    tax_id: string | null;
    profile: IWholesalerProfile;
  }> {
    return this.wholesalerProfileService.getWholesalerProfile(
      req.user.userId,
      req.ability,
    );
  }
}
