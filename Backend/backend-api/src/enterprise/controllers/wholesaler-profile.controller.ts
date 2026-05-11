import { Controller, Req, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { seconds, Throttle } from '@nestjs/throttler';
import { RolesAllowed } from '#/common/guards/decorator/roles-allowed.decorator.js';
import { UserRole } from '#/generated/drizzle/enums.js';
import { WholesalerProfileService } from '#/enterprise/services/wholesaler-profile.service.js';
import { TypedRoute } from '@nestia/core';
import { FastifyRequest } from 'fastify';
import {
  IUpdateWholesalerProfileDto,
  validateUpdateWholesalerProfile,
} from '#/enterprise/dto/update-wholesaler-profile.dto.js';
import { TypedBody } from '#/utils/typia/typed-body.typia.js';
import { JwtAuthGuard } from '#/auth/guard/auth.guard.js';
import { IWholesalerProfile } from '#/enterprise/types/IWholesalerProfile.js';

@ApiTags('Employee Management')
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
    id: string;
    email: string;
    user_id: string | null;
    profile_image_file_id: bigint | null;
    first_name: string | null;
    last_name: string | null;
    username: string | null;
    telephone: string | null;
    tax_id: string | null;
    profile: IWholesalerProfile;
    store_directions: {
      street: string;
      zip_code: string;
      country: {
        name: string;
        name_local: string;
        iso_numeric: number;
      };
      province: {
        id: number;
        name: string;
        name_local: string;
      };
      city: {
        id: number;
        name: string;
        name_local: string;
      };
    };
  }> {
    return this.wholesalerProfileService.getWholesalerProfile(
      req.user.userId,
      req.ability,
    );
  }
}
