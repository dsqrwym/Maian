import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  Patch,
  Post,
  Query,
  Req,
  UseGuards,
} from '@nestjs/common';
import { CategoryService } from '../services/category.service';
import { CreateCategoryDto } from '../dto/create-category.dto';
import { UpdateCategoryDto } from '../dto/update-category.dto';
import { JwtAuthGuard } from '../../auth/guard/auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';
import { FastifyRequest } from 'fastify';
import { RolesAllowed } from '../../common/guards/decorator/roles-allowed.decorator';
import { UserRole } from 'src/generated/prisma/client';
import { CategoryQueryDto } from '../dto/category-query.dto';
import {
  ApiBearerAuth,
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiParam,
  ApiQuery,
} from '@nestjs/swagger';
import { seconds, Throttle } from '@nestjs/throttler';

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
@Throttle({ default: { limit: 10, ttl: seconds(1) } })
@UseGuards(JwtAuthGuard, RolesGuard)
export class CategoryController {
  constructor(private readonly categoryService: CategoryService) {}

  /**
   * Create a new category
   * @param {CreateCategoryDto} createCategoryDto - Category data
   * @param {FastifyRequest} req - Request object containing user ability
   */
  @Post()
  @ApiOperation({ summary: 'Create a new category' })
  @ApiResponse({
    status: 201,
    description: 'Category successfully created',
  })
  @ApiResponse({
    status: 400,
    description: 'Invalid input data',
  })
  @RolesAllowed(
    UserRole.WHOLESALER,
    UserRole.DELIVERY,
    UserRole.SUPPORT,
    UserRole.WAREHOUSE,
    UserRole.ADMIN,
    UserRole.SUPERADMIN,
  )
  async create(
    @Req() req: FastifyRequest,
    @Body() createCategoryDto: CreateCategoryDto,
  ): Promise<void> {
    return this.categoryService.create(
      createCategoryDto,
      req.ability,
      req.user,
    );
  }

  /**
   * Search and filter categories
   * @param {CategoryQueryDto} query - Search criteria
   * @param req
   */
  @Get()
  @ApiOperation({ summary: 'Search and filter categories' })
  @ApiResponse({
    status: 200,
    description: 'Successfully retrieved categories',
  })
  @ApiQuery({ type: CategoryQueryDto })
  async search(
    @Query() query: CategoryQueryDto,
    @Req() req: FastifyRequest,
  ): Promise<unknown> {
    return this.categoryService.search(query, req.ability);
  }

  /**
   * Get category data for update
   * @param {string} id - Category ID
   * @param {FastifyRequest} req - Request object containing user ability
   * @returns {Promise<unknown>}
   */
  @Get(':id/update')
  @ApiOperation({ summary: 'Get category data for update' })
  @ApiParam({
    name: 'id',
    description: 'ID of the category to fetch for update',
    type: 'string',
    example: '1234567890123456',
  })
  @ApiResponse({
    status: 200,
    description: 'Category successfully retrieved for update',
  })
  @ApiResponse({
    status: 404,
    description: 'Category not found',
  })
  @RolesAllowed(
    UserRole.WHOLESALER,
    UserRole.WAREHOUSE,
    UserRole.ADMIN,
    UserRole.SUPERADMIN,
  )
  async getForUpdate(
    @Param('id') id: string,
    @Req() req: FastifyRequest,
  ): Promise<unknown> {
    return this.categoryService.getCategoryForUpdate(id, req.ability);
  }

  /**
   * Update an existing category
   * @param {UpdateCategoryDto} updateCategoryDto - Category data to update
   * @param {FastifyRequest} req - Request object containing user ability
   */
  @Patch()
  @ApiOperation({ summary: 'Update an existing category' })
  @ApiResponse({
    status: 200,
    description: 'Category successfully updated',
  })
  @ApiResponse({
    status: 404,
    description: 'Category not found',
  })
  @RolesAllowed(
    UserRole.WHOLESALER,
    UserRole.WAREHOUSE,
    UserRole.ADMIN,
    UserRole.SUPERADMIN,
  )
  async update(
    @Req() req: FastifyRequest,
    @Body() updateCategoryDto: UpdateCategoryDto,
  ): Promise<void> {
    return this.categoryService.update(
      updateCategoryDto,
      req.ability,
      req.user,
    );
  }

  /**
   * Delete a category by ID
   * @param {bigint} id - ID of the category to delete
   * @param {FastifyRequest} req - Request object containing user ability
   * @returns {Promise<void>}
   */
  @Delete(':id')
  @ApiOperation({ summary: 'Delete a category' })
  @ApiParam({
    name: 'id',
    description: 'ID of the category to delete',
    type: 'string',
    example: '1234567890123456',
  })
  @ApiResponse({
    status: 200,
    description: 'Category successfully deleted',
  })
  @ApiResponse({
    status: 404,
    description: 'Category not found',
  })
  @RolesAllowed(
    UserRole.WHOLESALER,
    UserRole.WAREHOUSE,
    UserRole.ADMIN,
    UserRole.SUPERADMIN,
  )
  async remove(
    @Param('id') id: bigint,
    @Req() req: FastifyRequest,
  ): Promise<void> {
    return this.categoryService.remove(id, req.ability);
  }
}
