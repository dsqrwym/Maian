import { Controller, Req, UseGuards } from '@nestjs/common';
import { CreateEmployeeService } from '../services/create-employee.service';
import {
  ICreateEmployeeDto,
  validateICreateEmployee,
} from '../dto/create-employee.dto';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '../../auth/guard/auth.guard';
import { FastifyRequest } from 'fastify';
import { RolesAllowed } from '../../common/guards/decorator/roles-allowed.decorator';
import { UserRole } from 'src/generated/prisma/client';
import { minutes, Throttle } from '@nestjs/throttler';
import { TypedBody } from '../../utils/typia/typed-body.typia';
import { TypedRoute } from '@nestia/core';

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
  @TypedRoute.Post('support')
  async createEmployee(
    @Req() req: FastifyRequest,
    @TypedBody(validateICreateEmployee)
    dto: ICreateEmployeeDto,
  ): Promise<void> {
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
  @TypedRoute.Post('delivery')
  async createDeliveryEmployee(
    @Req() req: FastifyRequest,
    @TypedBody(validateICreateEmployee)
    dto: ICreateEmployeeDto,
  ): Promise<void> {
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
  @TypedRoute.Post('warehouse')
  async createWarehouseEmployee(
    @Req() req: FastifyRequest,
    @TypedBody(validateICreateEmployee)
    dto: ICreateEmployeeDto,
  ): Promise<void> {
    const wholesalerId = req.user.userId;
    return this.wholesalerService.createWarehouseEmployee(wholesalerId, dto);
  }
}
