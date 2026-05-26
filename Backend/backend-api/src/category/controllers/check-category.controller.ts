import { Controller, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { seconds, Throttle } from '@nestjs/throttler';
import { CheckCategoryService } from '../services/check-category.service.js';
import {
  ICheckCategoryNameCreateQueryDto,
  ICheckCategoryNameUpdateQueryDto,
  validateCheckCategoryNameCreateQuery,
  validateCheckCategoryNameUpdateQuery,
} from '../dto/check-category-query.dto.js';
import { TypedQuery, TypedRoute } from '@nestia/core';
import { JwtAuthGuard } from '#/auth/guard/auth.guard.js';
import { RolesGuard } from '#/common/guards/roles.guard.js';
import { RolesAllowed } from '#/common/guards/decorator/roles-allowed.decorator.js';
import { ADMIN_ROLES } from '#/admin/admin.constants.js';
import { UserRole } from '#/generated/drizzle/enums.js';

/**
 * Controller for checking category name availability.
 *
 * @class CheckCategoryController
 */
@ApiTags('Category Management')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard, RolesGuard)
@Throttle({ default: { limit: 2, ttl: seconds(1) } })
@RolesAllowed(
  UserRole.WHOLESALER,
  UserRole.DELIVERY,
  UserRole.SUPPORT,
  UserRole.WAREHOUSE,
  ...ADMIN_ROLES,
)
@Controller('check')
export class CheckCategoryController {
  constructor(private readonly checkCategoryService: CheckCategoryService) {}

  /**
   * Check if a category name is already used for creating.
   *
   * Returns true if the name is already taken, false if available.
   *
   * @param {ICheckCategoryNameCreateQueryDto} query - Contains name and optional parentId/userId
   * @returns {Promise<boolean>} Whether the name is already used
   */
  @TypedRoute.Get('name')
  async checkNameForCreate(
    @TypedQuery(validateCheckCategoryNameCreateQuery)
    query: ICheckCategoryNameCreateQueryDto,
  ): Promise<boolean> {
    return this.checkCategoryService.checkNameUsedForCreate(query);
  }

  /**
   * Check if a category name is already used for updating (excluding current ID).
   *
   * Returns true if the name is already taken by another category, false if available.
   *
   * @param {ICheckCategoryNameUpdateQueryDto} query - Contains name, id, and optional parentId/userId
   * @returns {Promise<boolean>} Whether the name is already used by another category
   */
  @TypedRoute.Get('name/update')
  async checkNameForUpdate(
    @TypedQuery(validateCheckCategoryNameUpdateQuery)
    query: ICheckCategoryNameUpdateQueryDto,
  ): Promise<boolean> {
    return this.checkCategoryService.checkNameUsedForUpdate(query);
  }
}
