import { Controller, Req, UseGuards } from '@nestjs/common';
import { WriteEmployeeService } from '../services/write-employee.service.js';
import {
  ICreateEmployeeDto,
  validateICreateEmployee,
} from '../dto/create-employee.dto.js';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '#/auth/guard/auth.guard.js';
import type { FastifyRequest } from 'fastify';
import { RolesAllowed } from '#/common/guards/decorator/roles-allowed.decorator.js';
import { UserRole } from '#/generated/drizzle/enums.js';
import { minutes, Throttle } from '@nestjs/throttler';
import { TypedBody } from '#/utils/typia/typed-body.typia.js';
import { TypedParam, TypedRoute } from '@nestia/core';
import {
  IUpdateEmployeeDto,
  validateIUpdateEmployee,
} from '#/enterprise/dto/update-employee.dto.js';
import { TagsUuid } from '#/utils/typia/validators/auth.validator.js';

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
@Controller('employees')
export class WriteEmployeeController {
  constructor(private readonly wholesalerService: WriteEmployeeService) {}

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

  /**
   * Update an existing employee account
   *
   * Allows a wholesaler to update basic employee profile information.
   *
   * Only employees belonging to the authenticated wholesaler can be updated.
   * Email address cannot be changed through this endpoint.
   *
   * Updatable fields:
   * - first_name
   * - last_name
   * - username
   * - telephone
   * - tax_id
   *
   * @param req - Contains the authenticated wholesaler information
   * @param id - Employee user ID
   * @param dto - Partial employee update data
   * @returns Empty response with 200 status code on success
   */
  @TypedRoute.Patch(':id')
  async updateEmployee(
    @Req() req: FastifyRequest,
    @TypedParam('id') id: TagsUuid,
    @TypedBody(validateIUpdateEmployee)
    dto: IUpdateEmployeeDto,
  ) {
    const wholesalerId = req.user.userId;
    return this.wholesalerService.updateEmployee(id, wholesalerId, dto);
  }

  /**
   * Delete an existing employee account
   *
   * Allows a wholesaler to delete an employee account.
   *
   * Only employees belonging to the authenticated wholesaler can be deleted.
   *
   * @param req - Contains the authenticated wholesaler information
   * @param id - Employee user ID
   * @returns Empty response with 200 status code on success
   */
  @TypedRoute.Delete(':id')
  async deleteEmployee(
    @Req() req: FastifyRequest,
    @TypedParam('id') id: TagsUuid,
  ) {
    const wholesalerId = req.user.userId;
    return this.wholesalerService.deleteEmployee(id, wholesalerId);
  }
}
