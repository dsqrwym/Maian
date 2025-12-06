import { Controller, Get, Query } from '@nestjs/common';
import { ApiOperation, ApiResponse, ApiTags } from '@nestjs/swagger';
import { seconds, Throttle } from '@nestjs/throttler';
import { CheckCategoryService } from '../services/check-category.service';
import {
  CheckCategoryNameCreateQueryDto,
  CheckCategoryNameUpdateQueryDto,
} from '../dto/check-category-query.dto';

/**
 * Category Availability Check Controller
 * 类别名称可用性检查控制器
 */
@ApiTags('Category Management')
@Throttle({ default: { limit: 2, ttl: seconds(1) } })
@Controller('check')
@ApiResponse({
  status: 200,
  description: 'Request processed successfully / 请求处理成功',
})
@ApiResponse({ status: 400, description: 'Bad request / 错误请求' })
@ApiResponse({ status: 429, description: 'Too Many Requests / 请求过于频繁' })
export class CheckCategoryController {
  constructor(private readonly checkCategoryService: CheckCategoryService) {}

  /**
   * Check category name availability for creating
   * 创建时检查类别名称是否已存在
   */
  @Get('name')
  @ApiOperation({
    summary: 'Check if category name already exists (create)',
    description:
      'Checks whether a category name already exists under a specific user (if userId provided) or in public scope (userId is null). Returns true if already exists, false otherwise. / 检查类别名称在指定用户下（提供userId）或公共范围（userId为空）是否已存在。若已存在返回true，否则返回false。',
  })
  @ApiResponse({
    status: 200,
    description: 'Category name availability status / 类别名称可用性状态',
    schema: {
      type: 'object',
      properties: {
        data: {
          type: 'boolean',
          description:
            'true if name already exists, false otherwise / 如果名称已存在为true，否则为false',
        },
      },
    },
  })
  async checkNameForCreate(@Query() query: CheckCategoryNameCreateQueryDto) {
    return this.checkCategoryService.checkNameUsedForCreate(query);
  }

  /**
   * Check category name availability for updating
   * 更新时检查类别名称是否已存在（排除当前ID）
   */
  @Get('name/update')
  @ApiOperation({
    summary: 'Check if category name already exists (update)',
    description:
      'Checks whether a category name already exists under the same scope excluding the current category ID. / 在相同范围内（用户或公共）检查类别名称是否已存在，排除当前类别ID。',
  })
  @ApiResponse({
    status: 200,
    description: 'Category name availability status / 类别名称可用性状态',
    schema: {
      type: 'object',
      properties: {
        data: {
          type: 'boolean',
          description:
            'true if name already exists, false otherwise / 如果名称已存在为true，否则为false',
        },
      },
    },
  })
  async checkNameForUpdate(@Query() query: CheckCategoryNameUpdateQueryDto) {
    return this.checkCategoryService.checkNameUsedForUpdate(query);
  }
}
