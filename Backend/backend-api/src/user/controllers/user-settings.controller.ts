import { Controller, Req, UseGuards } from '@nestjs/common';
import {
  ApiBadRequestResponse,
  ApiBearerAuth,
  ApiOkResponse,
  ApiOperation,
  ApiTags,
  ApiTooManyRequestsResponse,
  ApiUnauthorizedResponse,
} from '@nestjs/swagger';
import { seconds, Throttle } from '@nestjs/throttler';
import { TypedRoute } from '@nestia/core';
import type { FastifyRequest } from 'fastify';

import { JwtAuthGuard } from '#/auth/guard/auth.guard.js';
import { TypedBody } from '#/utils/typia/typed-body.typia.js';
import {
  IUpdateUserLanguageDto,
  validateUpdateUserLanguage,
} from '#/user/dto/update-user-language.dto.js';
import type { UserSettingsResponseDto } from '#/user/dto/user-settings-response.dto.js';
import { UserSettingsService } from '#/user/services/user-settings.service.js';

@ApiTags('User')
@ApiBearerAuth()
@ApiUnauthorizedResponse({
  description: 'Unauthorized: Authentication required',
})
@ApiTooManyRequestsResponse({
  description: 'Too many requests, please try again later',
})
@UseGuards(JwtAuthGuard)
@Throttle({ default: { limit: 5, ttl: seconds(1) } })
@Controller('settings')
export class UserSettingsController {
  constructor(private readonly userSettingsService: UserSettingsService) {}

  @ApiOperation({
    summary: 'Get current user settings',
    description: 'Returns the settings for the currently authenticated user',
  })
  @ApiOkResponse({
    description: 'Successfully retrieved user settings',
  })
  @TypedRoute.Get()
  async getSettings(
    @Req() req: FastifyRequest,
  ): Promise<UserSettingsResponseDto> {
    return this.userSettingsService.getSettings(req.user.userId);
  }

  @ApiOperation({
    summary: 'Update current user language',
    description:
      'Updates the language setting for the currently authenticated user',
  })
  @ApiOkResponse({
    description: 'Successfully updated user language',
  })
  @ApiBadRequestResponse({
    description: 'Bad request: Invalid language',
  })
  @TypedRoute.Patch('language')
  async updateLanguage(
    @Req() req: FastifyRequest,
    @TypedBody(validateUpdateUserLanguage) body: IUpdateUserLanguageDto,
  ): Promise<void> {
    return this.userSettingsService.updateLanguage(req.user.userId, body);
  }
}
