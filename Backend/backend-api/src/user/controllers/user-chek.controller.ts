import { Controller, Get, Query } from '@nestjs/common';
import { UserCheckService } from '../services/user-check.service';
import { seconds, Throttle } from '@nestjs/throttler';
import { CacheTTL } from '@nestjs/cache-manager';
import { SECOND } from '../../utils/date.utils';
import { ApiOperation, ApiQuery, ApiResponse, ApiTags } from '@nestjs/swagger';

@ApiTags('User')
@Throttle({ default: { limit: 1, ttl: seconds(1) } })
@CacheTTL(30 * SECOND)
@Controller('user/check')
@ApiResponse({ status: 200, description: 'Request processed successfully' })
@ApiResponse({ status: 400, description: 'Bad request' })
@ApiResponse({ status: 429, description: 'Too Many Requests' })
export class UserChekController {
  constructor(private readonly userCheckService: UserCheckService) {}
  @Get('mail')
  @ApiOperation({
    summary: 'Check if email is already registered',
    description: 'Returns true if the email is already in use, false otherwise',
  })
  @ApiQuery({
    name: 'email',
    required: true,
    description: 'Email address to check',
    example: 'user@example.com',
  })
  @ApiResponse({
    status: 200,
    description: 'Email availability status',
    schema: {
      type: 'object',
      properties: {
        used: { type: 'boolean' },
      },
    },
  })
  async checkMailUsed(@Query('email') email: string) {
    return this.userCheckService.checkMailUsed(email);
  }
  @Get('username')
  @ApiOperation({
    summary: 'Check if username is already taken',
    description:
      'Returns true if the username is already in use, false otherwise',
  })
  @ApiQuery({
    name: 'username',
    required: true,
    description: 'Username to check',
    example: 'johndoe',
  })
  @ApiResponse({
    status: 200,
    description: 'Username availability status',
    schema: {
      type: 'object',
      properties: {
        used: { type: 'boolean' },
      },
    },
  })
  async checkUsernameUsed(@Query('username') username: string) {
    return this.userCheckService.checkUsernameUsed(username);
  }
}
