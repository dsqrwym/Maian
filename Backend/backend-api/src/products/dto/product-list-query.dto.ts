import {
  IsEnum,
  IsNotEmpty,
  IsOptional,
  IsString,
  IsUUID,
} from 'class-validator';
import { PaginationQueryDto } from '../../utils/dto/pagination.dto';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Trim } from '../../utils/transform/trim.decorator';
import { ProductStatus } from 'src/generated/prisma/client';
import { ProductListSelectField } from '../product.enums';

export class ProductListQueryDto extends PaginationQueryDto {
  // --- 搜索和过滤 ---
  @ApiProperty({ description: 'Keywords for name search', required: false })
  @IsString()
  @IsOptional()
  @Trim()
  search?: string; // 搜索关键字 (用于 name, title, product_code)

  @ApiProperty({
    description: 'Filter by language code (e.g., en, es)',
    required: false,
  })
  @IsString()
  @IsOptional()
  langCode?: string; // 用于指定返回 lang 中的哪个字段

  @ApiPropertyOptional({
    description: 'Filter by primary or related category ID',
    example: 'cat_123',
  })
  @IsOptional()
  @IsString()
  @IsNotEmpty()
  category_id?: string; // 按主分类或关联分类过滤

  @ApiPropertyOptional({
    description: 'Filter products by wholesaler ID (UUID)',
    format: 'uuid',
  })
  @IsOptional()
  @IsUUID()
  wholesaler_id?: string; // 零售商想过滤特定批发商的产品 (仅对零售商开放)

  @ApiPropertyOptional({
    description: 'Field to sort by',
    enum: ['name', 'product_code', 'available_stock', 'price_iva', 'price'],
    default: 'name',
  })
  @IsOptional()
  @IsString()
  @IsEnum(['name', 'product_code', 'available_stock', 'price_iva', 'price'])
  sort_by: string = 'name';

  @ApiPropertyOptional({
    description: 'Sort order direction',
    enum: ['asc', 'desc'],
    default: 'asc',
  })
  @IsOptional()
  @IsEnum(['asc', 'desc'])
  sort_order: 'asc' | 'desc' = 'asc';

  @ApiPropertyOptional({
    description: 'Filter by product status',
    enum: ProductStatus,
  })
  @IsOptional()
  @IsEnum(ProductStatus)
  status?: ProductStatus;

  // select
  @ApiProperty({
    description: 'Selected fields',
    enum: ProductListSelectField,
    required: false,
  })
  @IsEnum(ProductListSelectField, {
    each: true,
  })
  @IsOptional()
  fields?: ProductListSelectField[];
}
