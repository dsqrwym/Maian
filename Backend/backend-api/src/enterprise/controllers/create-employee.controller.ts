import { Body, Controller, Post, Req, UseGuards } from '@nestjs/common';
import { CreateEmployeeService } from '../services/create-employee.service';
import { CreateEmployeeDto } from '../dto/create-employee.dto';
import {
  ApiBadRequestResponse,
  ApiBearerAuth,
  ApiConflictResponse,
  ApiForbiddenResponse,
  ApiOperation,
  ApiResponse,
  ApiTags,
  ApiUnauthorizedResponse,
} from '@nestjs/swagger';
import { JwtAuthGuard } from '../../auth/guard/auth.guard';
import { FastifyRequest } from 'fastify';
import { RolesAllowed } from '../../common/guards/decorator/roles-allowed.decorator';
import { UserRole } from 'src/generated/prisma/client';
import { minutes, Throttle } from '@nestjs/throttler';

/**
 * Controller for creating different types of employee accounts
 * Requires WHOLESALER role and valid JWT token
 *
 * All endpoints create employees with PENDING_VERIFICATION status and send verification emails.
 * The verification links are valid for 7 days.
 */
@ApiTags('Employee Management')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@RolesAllowed(UserRole.WHOLESALER)
@Throttle({ default: { limit: 5, ttl: minutes(1) } })
@Controller('create-employee')
export class CreateEmployeeController {
  constructor(private readonly wholesalerService: CreateEmployeeService) {}

  /**
   * Create a new support employee
   *
   * Creates a support employee account with PENDING_VERIFICATION status.
   * The employee will receive a verification email with a link to activate their account.
   * The username is automatically generated as: {wholesaler_user_id}@{random_uuid}
   *
   * @param req - Contains the authenticated user's information
   * @param dto - Employee details including email, name, and contact information
   * @returns Empty response with 201 status code on success
   */
  @ApiOperation({
    summary: 'Create a support employee',
    description: `Creates a support employee account with PENDING_VERIFICATION status.
    The employee will receive a verification email with a link to activate their account.
    The username is automatically generated.`,
  })
  @ApiResponse({
    status: 201,
    description:
      'Support employee created successfully. Verification email sent.',
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
    description: 'Missing or invalid JWT token',
    schema: {
      example: {
        statusCode: 401,
        message: 'Unauthorized',
      },
    },
  })
  @ApiForbiddenResponse({
    description: 'User does not have WHOLESALER role',
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
  @Post('support')
  async createEmployee(
    @Req() req: FastifyRequest,
    @Body() dto: CreateEmployeeDto,
  ) {
    const wholesalerId = req.user.userId;
    return this.wholesalerService.createSupportEmployee(wholesalerId, dto);
  }

  /**
   * Create a new delivery employee
   *
   * Creates a delivery employee account with PENDING_VERIFICATION status.
   * The employee will receive a verification email with a link to activate their account.
   * The username is automatically generated as: {wholesaler_user_id}@{random_uuid}
   *
   * @param req - Contains the authenticated user's information
   * @param dto - Employee details including email, name, and contact information
   * @returns Empty response with 201 status code on success
   */
  @ApiOperation({
    summary: 'Create a delivery employee',
    description: `Creates a delivery employee account with PENDING_VERIFICATION status.
    The employee will receive a verification email with a link to activate their account.
    The username is automatically generated.`,
  })
  @ApiResponse({
    status: 201,
    description:
      'Delivery employee created successfully. Verification email sent.',
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
    description: 'Missing or invalid JWT token',
    schema: {
      example: {
        statusCode: 401,
        message: 'Unauthorized',
      },
    },
  })
  @ApiForbiddenResponse({
    description: 'User does not have WHOLESALER role',
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
  @Post('delivery')
  async createDeliveryEmployee(
    @Req() req: FastifyRequest,
    @Body() dto: CreateEmployeeDto,
  ) {
    const wholesalerId = req.user.userId;
    return this.wholesalerService.createDeliveryEmployee(wholesalerId, dto);
  }

  /**
   * Create a new warehouse employee
   *
   * Creates a warehouse employee account with PENDING_VERIFICATION status.
   * The employee will receive a verification email with a link to activate their account.
   * The username is automatically generated as: {wholesaler_user_id}@{random_uuid}
   *
   * @param req - Contains the authenticated user's information
   * @param dto - Employee details including email, name, and contact information
   * @returns Empty response with 201 status code on success
   */
  @ApiOperation({
    summary: 'Create a warehouse employee',
    description: `Creates a warehouse employee account with PENDING_VERIFICATION status.
    The employee will receive a verification email with a link to activate their account.
    The username is automatically generated.`,
  })
  @ApiResponse({
    status: 201,
    description:
      'Warehouse employee created successfully. Verification email sent.',
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
    description: 'Missing or invalid JWT token',
    schema: {
      example: {
        statusCode: 401,
        message: 'Unauthorized',
      },
    },
  })
  @ApiForbiddenResponse({
    description: 'User does not have WHOLESALER role',
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
  @Post('warehouse')
  async createWarehouseEmployee(
    @Req() req: FastifyRequest,
    @Body() dto: CreateEmployeeDto,
  ) {
    const wholesalerId = req.user.userId;
    return this.wholesalerService.createWarehouseEmployee(wholesalerId, dto);
  }
}
