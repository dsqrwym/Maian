import { Body, Controller, Post, UseGuards } from '@nestjs/common';
import { CreateAdminService } from '../services/create-admin.service';
import { CreateAdminDto } from '../dto/create-admin.dto';
import {
  ApiBadRequestResponse,
  ApiBearerAuth,
  ApiConflictResponse,
  ApiCreatedResponse,
  ApiForbiddenResponse,
  ApiOperation,
  ApiTags,
  ApiUnauthorizedResponse,
} from '@nestjs/swagger';
import { JwtAuthGuard } from '../../auth/guard/auth.guard';
import { RolesAllowed } from '../../common/guards/decorator/roles-allowed.decorator';
import { UserRole } from '@prisma/client';

/**
 * Controller for creating new admin users
 * Requires ADMIN role and valid JWT token
 *
 * @endpoint POST /create-admin
 */
@ApiTags('Admin Management')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@RolesAllowed(UserRole.SUPERADMIN)
@Controller('create-admin')
export class CreateAdminController {
  constructor(private readonly createAdminService: CreateAdminService) {}

  /**
   * Create a new admin user
   *
   * This endpoint creates a new admin user with PENDING_VERIFICATION status.
   * An email with verification link will be sent to the provided email address.
   * The verification link is valid for 7 days.
   *
   * @param dto - Contains email and optional username for the new admin
   * @returns Empty response with 201 status code on success
   */
  @ApiOperation({
    summary: 'Create a new admin user',
    description: `Creates a new admin user with PENDING_VERIFICATION status.
    An email with verification link will be sent to the provided email address.
    The verification link is valid for 7 days.`,
  })
  @ApiCreatedResponse({
    description: 'Admin user created successfully. Verification email sent.',
  })
  @ApiBadRequestResponse({
    description: 'Invalid input data',
    schema: {
      example: {
        statusCode: 400,
        message: ['email must be an email'],
        error: 'Bad Request',
      },
    },
  })
  @ApiUnauthorizedResponse({
    description: 'Unauthorized - Missing or invalid JWT token',
    schema: {
      example: {
        statusCode: 401,
        message: 'Unauthorized',
      },
    },
  })
  @ApiForbiddenResponse({
    description: 'Forbidden - User does not have ADMIN role',
    schema: {
      example: {
        statusCode: 403,
        message: 'Forbidden resource',
        error: 'Forbidden',
      },
    },
  })
  @ApiConflictResponse({
    description: 'Email already exists in the system',
    schema: {
      example: {
        statusCode: 409,
        message: 'Email already exists',
        error: 'Conflict',
      },
    },
  })
  @Post()
  async createAdmin(@Body() dto: CreateAdminDto) {
    return this.createAdminService.createAdmin(dto);
  }
}
