import { Controller, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { seconds, Throttle } from '@nestjs/throttler';
import { CheckCategoryService } from '../services/check-category.service';
import {
  ICheckCategoryNameCreateQueryDto,
  ICheckCategoryNameUpdateQueryDto,
  validateCheckCategoryNameCreateQuery,
  validateCheckCategoryNameUpdateQuery,
} from '../dto/check-category-query.dto';
import { TypedQuery, TypedRoute } from '@nestia/core';
import { JwtAuthGuard } from '../../auth/guard/auth.guard';
import { RolesGuard } from '../../common/guards/roles.guard';
import { RolesAllowed } from '../../common/guards/decorator/roles-allowed.decorator';
import { UserRole } from '../../generated/prisma/enums';
import { ADMIN_ROLES } from '../../admin/admin.constants';

/**
 * Category Availability Check Controller
 * 类别名称可用性检查控制器
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
   * Check category name availability for creating
   * 创建时检查类别名称是否已存在
   */
  @TypedRoute.Get('name')
  async checkNameForCreate(
    @TypedQuery(validateCheckCategoryNameCreateQuery)
    query: ICheckCategoryNameCreateQueryDto,
  ): Promise<boolean> {
    return this.checkCategoryService.checkNameUsedForCreate(query);
  }

  /**
   * Check category name availability for updating
   * 更新时检查类别名称是否已存在（排除当前ID）
   */
  @TypedRoute.Get('name/update')
  async checkNameForUpdate(
    @TypedQuery(validateCheckCategoryNameUpdateQuery)
    query: ICheckCategoryNameUpdateQueryDto,
  ): Promise<boolean> {
    return this.checkCategoryService.checkNameUsedForUpdate(query);
  }
}
