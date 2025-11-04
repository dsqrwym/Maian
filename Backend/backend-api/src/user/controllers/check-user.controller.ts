import { Controller, Get, Query } from '@nestjs/common';
import { CheckUserService } from '../services/check-user.service';
import { seconds, Throttle } from '@nestjs/throttler';
import { CacheTTL } from '@nestjs/cache-manager';
import { SECOND } from '../../utils/date.utils';
import { ApiOperation, ApiQuery, ApiResponse, ApiTags } from '@nestjs/swagger';
import {
  CheckUserEmailQueryDto,
  CheckUserUsernameQueryDto,
} from '../dto/check-user-query.dto';

/**
 * User Availability Check Controller
 * 用户可用性检查控制器
 *
 * This controller handles endpoints for checking the availability of user credentials
 * 该控制器处理检查用户凭据可用性的端点
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
   * Check email availability
   * 检查邮箱可用性
   *
   * @param query - Query parameters containing email to check
   * @param query - 包含待检查邮箱的查询参数
   * @returns Object containing availability status
   * @returns 包含可用性状态的对象
   */
  @Get('mail')
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
  async checkEmailUsed(@Query() query: CheckUserEmailQueryDto) {
    return this.userCheckService.checkEmailUsed(query.email);
  }

  /**
   * Check username availability
   * 检查用户名可用性
   *
   * @param query - Query parameters containing username to check
   * @param query - 包含待检查用户名的查询参数
   * @returns Object containing availability status
   * @returns 包含可用性状态的对象
   */
  @Get('username')
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
  async checkUsernameUsed(@Query() query: CheckUserUsernameQueryDto) {
    return this.userCheckService.checkUsernameUsed(query);
  }
}
