import { Controller, Req, UseGuards } from '@nestjs/common';
import { CheckUserService } from '../services/check-user.service.js';
import { seconds, Throttle } from '@nestjs/throttler';
import { CacheTTL } from '@nestjs/cache-manager';
import { SECOND } from '#/utils/date.utils.js';
import {
  ApiBadRequestResponse,
  ApiBearerAuth,
  ApiForbiddenResponse,
  ApiOkResponse,
  ApiOperation,
  ApiQuery,
  ApiResponse,
  ApiTags,
  ApiTooManyRequestsResponse,
  ApiUnauthorizedResponse,
} from '@nestjs/swagger';
import {
  ICheckUserEmailQueryDto,
  ICheckUserTaxIdQueryDto,
  ICheckUserUsernameQueryDto,
  validateICheckUserEmailQueryDto,
  validateICheckUserTaxIdQueryDto,
  validateICheckUserUsernameQueryDto,
} from '../dto/check-user-query.dto.js';
import { TypedQuery, TypedRoute } from '@nestia/core';
import { FastifyRequest } from 'fastify';
import { JwtAuthGuard } from '#/auth/guard/auth.guard.js';

/**
 * Controller for checking user credential availability (email and username)
 * @class CheckUserController
 */
@ApiTags('User')
@Throttle({ default: { limit: 2, ttl: seconds(1) } })
@CacheTTL(30 * SECOND)
@Controller('check')
@ApiResponse({
  status: 200,
  description: 'Request processed successfully / 请求处理成功',
})
@ApiResponse({
  status: 400,
  description: 'Bad request / 错误请求',
})
@ApiResponse({
  status: 429,
  description: 'Too Many Requests / 请求过于频繁',
})
export class CheckUserController {
  constructor(private readonly userCheckService: CheckUserService) {}
  /**
   * Check if an email is already registered.
   *
   * Returns true if the email is already in use, false otherwise.
   *
   * @param {ICheckUserEmailQueryDto} query - Contains email to check
   * @returns {Promise<boolean>} Whether the email is already registered
   */
  @TypedRoute.Get('mail')
  @ApiOperation({
    summary: 'Check if email is already registered',
    description:
      'Check if the provided email address is already in use in the system. Returns true if email is already registered, false otherwise. / 检查提供的邮箱地址是否已被注册。如果邮箱已被注册返回true，否则返回false。',
  })
  @ApiQuery({
    name: 'email',
    required: true,
    description:
      'Email address to check for availability / 需要检查可用性的邮箱地址',
    example: 'user@example.com',
  })
  @ApiResponse({
    status: 200,
    description: 'Email availability status / 邮箱可用性状态',
    schema: {
      type: 'object',
      properties: {
        data: {
          type: 'boolean',
          description:
            'true if email is already in use, false otherwise / 如果邮箱已被使用则为true，否则为false',
        },
      },
    },
  })
  async checkEmailUsed(
    @TypedQuery(validateICheckUserEmailQueryDto) query: ICheckUserEmailQueryDto,
  ): Promise<boolean> {
    return this.userCheckService.checkEmailUsed(query.email);
  }

  /**
   * Check if a username is already taken.
   *
   * Returns true if the username is already in use, false otherwise.
   * Usernames must be 3-30 characters long.
   *
   * @param {ICheckUserUsernameQueryDto} query - Contains username to check
   * @returns {Promise<boolean>} Whether the username is already taken
   */
  @TypedRoute.Get('username')
  @ApiOperation({
    summary: 'Check if username is already taken',
    description:
      'Check if the provided username is already in use. Returns true if username is taken, false otherwise. Usernames must be 3-30 characters long. / 检查提供的用户名是否已被使用。如果用户名已被使用返回true，否则返回false。用户名长度需在3-30个字符之间。',
  })
  @ApiQuery({
    name: 'username',
    required: true,
    description:
      'Username to check for availability (3-30 characters) / 需要检查可用性的用户名(3-30个字符)',
    example: 'johndoe',
  })
  @ApiResponse({
    status: 200,
    description: 'Username availability status / 用户名可用性状态',
    schema: {
      type: 'object',
      properties: {
        data: {
          type: 'boolean',
          description:
            'true if username is already taken, false otherwise / 如果用户名已被使用则为true，否则为false',
        },
      },
    },
  })
  async checkUsernameUsed(
    @TypedQuery(validateICheckUserUsernameQueryDto)
    query: ICheckUserUsernameQueryDto,
  ): Promise<boolean> {
    return this.userCheckService.checkUsernameUsed(query);
  }

  /**
   * Check if an tax id is already registered.
   *
   * Returns true if the tax id is already in use, false otherwise.
   *
   * @param {ICheckUserTaxIdQueryDto} query - Contains tax id to check, id to skip default use current user
   * @param req
   * @returns {Promise<boolean>} Whether the tax id is already registered
   */
  @ApiBearerAuth()
  @UseGuards(JwtAuthGuard)
  @TypedRoute.Get('tax_id')
  @ApiOperation({
    summary: 'Check if tax_id is already used by same role',
  })
  async checkUserTaxIdUsed(
    @TypedQuery(validateICheckUserTaxIdQueryDto) query: ICheckUserTaxIdQueryDto,
    @Req() req: FastifyRequest,
  ): Promise<boolean> {
    return this.userCheckService.checkUserTaxId(
      query.taxId,
      query.id ?? req.user.userId,
      req.user.userRole,
    );
  }
}
