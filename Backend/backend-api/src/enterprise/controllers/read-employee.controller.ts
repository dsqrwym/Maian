import { TypedParam, TypedQuery, TypedRoute } from '@nestia/core';
import { Controller, Req, UseGuards } from '@nestjs/common';
import { FastifyRequest } from 'fastify';
import {
  IFindEmployeeQuery,
  validateFindEmployeeQuery,
} from '#/enterprise/dto/find-employee-query.js';
import { TagsUuid } from '#/utils/typia/validators/auth.validator.js';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '#/auth/guard/auth.guard.js';
import { RolesAllowed } from '#/common/guards/decorator/roles-allowed.decorator.js';
import { UserRole, UserStatus } from '#/generated/drizzle/enums.js';
import { ReadEmployeeService } from '#/enterprise/services/read-employee.service.js';
import { PaginationMetaDto } from '#/utils/dto/pagination.dto.js';

@ApiTags('Employee Management')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@RolesAllowed(UserRole.WHOLESALER)
@Controller('employees')
export class ReadEmployeeController {
  constructor(private readonly readEmployeeService: ReadEmployeeService) {}

  /**
   * Get wholesaler employees list
   *
   * Returns paginated employees belonging to the authenticated wholesaler.
   *
   * Supports:
   * - pagination
   * - search
   * - sorting
   * - role filtering
   *
   * @param req - Authenticated wholesaler request
   * @param query - Employee search and pagination query
   * @returns Paginated employee list
   */
  @TypedRoute.Get()
  async findAllEmployees(
    @Req() req: FastifyRequest,
    @TypedQuery(validateFindEmployeeQuery)
    query: IFindEmployeeQuery,
  ): Promise<{
    items: {
      id: string;
      user_id: string | null;
      first_name: string | null;
      last_name: string | null;
      email: string;
      username: string | null;
      telephone: string | null;
      tax_id: string | null;
      role: UserRole;
      status: UserStatus;
    }[];
    pagination: PaginationMetaDto;
  }> {
    const wholesalerId = req.user.userId;

    return this.readEmployeeService.findAllEmployees(query, wholesalerId);
  }

  /**
   * Get employee information for update form
   *
   * Returns editable employee fields.
   * Only employees belonging to the authenticated wholesaler can be accessed.
   *
   * @param req - Authenticated wholesaler request
   * @param id - Employee user ID
   * @returns Employee editable information
   */
  @TypedRoute.Get(':id')
  async getEmployeeForUpdate(
    @Req() req: FastifyRequest,
    @TypedParam('id') id: TagsUuid,
  ): Promise<{
    first_name: string | null;
    last_name: string | null;
    username: string | null;
    telephone: string | null;
    tax_id: string | null;
  }> {
    const wholesalerId = req.user.userId;

    return this.readEmployeeService.getForUpdate(id, wholesalerId);
  }
}
