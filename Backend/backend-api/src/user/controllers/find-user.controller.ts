import { Controller, Get, Query, Req, UseGuards } from '@nestjs/common';
import { seconds, Throttle } from '@nestjs/throttler';
import {
  ApiBadRequestResponse,
  ApiBearerAuth,
  ApiForbiddenResponse,
  ApiOkResponse,
  ApiOperation,
  ApiQuery,
  ApiTags,
  ApiTooManyRequestsResponse,
  ApiUnauthorizedResponse,
} from '@nestjs/swagger';
import { FindUserService } from '../services/find-user.service';
import { FindUserQueryDto } from '../dto/find-user-query.dto';
import { FastifyRequest } from 'fastify';
import { JwtAuthGuard } from '../../auth/guard/auth.guard';
import { UserRole, UserStatus } from '@prisma/client';
import { PaginatedResponseDto } from '../../utils/dto/pagination.dto';

@ApiTags('User')
@ApiBearerAuth()
@ApiOkResponse({
  description: 'Successfully retrieved user(s)',
})
@ApiBadRequestResponse({
  description: 'Bad request: Invalid query parameters',
})
@ApiUnauthorizedResponse({
  description: 'Unauthorized: Authentication required',
})
@ApiForbiddenResponse({
  description: 'Forbidden: Insufficient permissions',
})
@ApiTooManyRequestsResponse({
  description: 'Too many requests, please try again later',
})
@UseGuards(JwtAuthGuard)
@Throttle({ default: { limit: 10, ttl: seconds(1) } })
@Controller()
export class FindUserController {
  constructor(private readonly findUserService: FindUserService) {}

  @ApiOperation({
    summary: 'Search users',
    description: 'Search and filter users with pagination',
  })
  @ApiQuery({
    name: 'search',
    required: false,
    description: 'Keywords for name search',
    type: String,
  })
  @ApiQuery({
    name: 'role',
    required: false,
    description: 'Filter by user role',
    enum: UserRole,
  })
  @ApiQuery({
    name: 'status',
    required: false,
    description: 'Filter by user status',
    enum: UserStatus,
  })
  @ApiQuery({
    name: 'page',
    required: false,
    description: 'Page number (starts from 1)',
    type: Number,
    example: 1,
  })
  @ApiQuery({
    name: 'limit',
    required: false,
    description: 'Number of items per page',
    type: Number,
    example: 50,
  })
  @ApiOkResponse({
    description: 'Paginated list of users',
    type: PaginatedResponseDto,
  })
  @Get()
  async findUser(@Query() query: FindUserQueryDto, @Req() req: FastifyRequest) {
    return this.findUserService.findUser(query, req.ability);
  }
}
