import { Controller, Get, Req, UseGuards } from '@nestjs/common';
import { CategoryService } from '#/category/services/category.service.js';
import {
  ICreateCategoryDto,
  validateCreateCategory,
} from '../dto/create-category.dto.js';
import {
  IUpdateCategoryDto,
  validateUpdateCategory,
} from '../dto/update-category.dto.js';
import { JwtAuthGuard } from '#/auth/guard/auth.guard.js';
import { RolesGuard } from '#/common/guards/roles.guard.js';
import type { FastifyRequest } from 'fastify';
import { RolesAllowed } from '#/common/guards/decorator/roles-allowed.decorator.js';
import {
  ICategoryQueryDto,
  validateCategoryQuery,
} from '../dto/category-query.dto.js';
import { ApiBearerAuth, ApiTags, ApiResponse } from '@nestjs/swagger';
import { seconds, Throttle } from '@nestjs/throttler';
import { ADMIN_ROLES } from '#/admin/admin.constants.js';
import { TypedParam, TypedQuery, TypedRoute } from '@nestia/core';
import { TypedBody } from '#/utils/typia/typed-body.typia.js';
import { PaginatedDataWithT } from '#/common/types-interfaces/response.interface.js';
import { ICategoryResponse } from '../dto/category-response.dto.js';
import { UserRole } from '#/generated/drizzle/enums.js';
import { TagsIntegerString } from '#/utils/typia/tags/string.tag.js';

/**
 * Controller for managing product categories
 * @class CategoryController
 */
@ApiTags('Category Management')
@ApiBearerAuth()
@ApiResponse({ status: 401, description: 'Unauthorized' })
@ApiResponse({
  status: 403,
  description: 'Forbidden. Insufficient permissions',
})
@Controller()
@UseGuards(JwtAuthGuard, RolesGuard)
@Throttle({ default: { limit: 10, ttl: seconds(1) } })
export class CategoryController {
  constructor(private readonly categoryService: CategoryService) {}

  /**
   * Create a new category.
   *
   * @param {ICreateCategoryDto} createCategoryDto - Category data
   * @param {FastifyRequest} req - Request object containing user ability
   * @returns {Promise<void>}
   */
  @TypedRoute.Post()
  @RolesAllowed(
    UserRole.WHOLESALER,
    UserRole.DELIVERY,
    UserRole.SUPPORT,
    UserRole.WAREHOUSE,
    ...ADMIN_ROLES,
  )
  async create(
    @Req() req: FastifyRequest,
    @TypedBody(validateCreateCategory) createCategoryDto: ICreateCategoryDto,
  ): Promise<void> {
    return this.categoryService.create(
      createCategoryDto,
      req.ability,
      req.user,
    );
  }

  /**
   * Search and filter categories.
   *
   * @param {ICategoryQueryDto} query - Search criteria
   * @param {FastifyRequest} req - Request object containing user ability
   * @returns {Promise<PaginatedDataWithT<ICategoryResponse>>} Paginated category list
   */
  @TypedRoute.Get()
  async search(
    @TypedQuery(validateCategoryQuery) query: ICategoryQueryDto,
    @Req() req: FastifyRequest,
  ): Promise<PaginatedDataWithT<ICategoryResponse>> {
    return this.categoryService.findAllUseDrizzle(query, req.ability, req.user);
  }

  /**
   * Update an existing category.
   *
   * Uses optimistic locking via version field to prevent concurrent modifications.
   *
   * @param {IUpdateCategoryDto} updateCategoryDto - Category data to update
   * @param {FastifyRequest} req - Request object containing user ability
   * @param {string} id - ID of the category to update
   * @returns {Promise<void>}
   */
  @ApiResponse({
    status: 409,
    description:
      'Version conflict. The category was modified by another request.',
  })
  @TypedRoute.Patch(':id')
  @RolesAllowed(UserRole.WHOLESALER, UserRole.WAREHOUSE, ...ADMIN_ROLES)
  async update(
    @Req() req: FastifyRequest,
    @TypedBody(validateUpdateCategory) updateCategoryDto: IUpdateCategoryDto,
    @TypedParam('id') id: TagsIntegerString,
  ): Promise<void> {
    return this.categoryService.update(
      id,
      updateCategoryDto,
      req.ability,
      req.user,
    );
  }

  /**
   * Get category data for update
   * @param {string} id - Category ID
   * @param {FastifyRequest} req - Request object containing user ability
   * @returns {Promise<{
   *     name: string;
   *     iva: string | null;
   *     version: bigint;
   *     translations: { name: string; lang_code: string }[];
   *   }>}
   */
  @Get(':id/update')
  @RolesAllowed(UserRole.WHOLESALER, UserRole.WAREHOUSE, ...ADMIN_ROLES)
  async getForUpdate(
    @TypedParam('id') id: TagsIntegerString,
    @Req() req: FastifyRequest,
  ): Promise<{
    name: string;
    iva: string | null;
    version: bigint;
    translations: { name: string; lang_code: string }[];
  }> {
    return this.categoryService.getCategoryForUpdate(id, req.ability);
  }

  /**
   * Delete a category by ID
   * @param {bigint} id - ID of the category to delete
   * @param {FastifyRequest} req - Request object containing user ability
   * @returns {Promise<void>}
   */
  @TypedRoute.Delete(':id')
  @RolesAllowed(UserRole.WHOLESALER, UserRole.WAREHOUSE, ...ADMIN_ROLES)
  async remove(
    @TypedParam('id') id: TagsIntegerString,
    @Req() req: FastifyRequest,
  ): Promise<void> {
    return this.categoryService.remove(id, req.ability);
  }
}
