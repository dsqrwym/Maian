import {
  IsEnum,
  IsInt,
  IsOptional,
  IsString,
  IsUUID,
  Min,
} from 'class-validator';
import { Type } from 'class-transformer';
import { PaginationQueryDto } from '../../utils/dto/pagination.dto';
import { ApiProperty } from '@nestjs/swagger';
import { Trim } from '../../utils/transform/trim.decorator';
import { ProductStatus } from '@prisma/client';
import { ProductListSelectField } from '../product.enums';

export class ProductQueryDto extends PaginationQueryDto {
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

  @IsOptional()
  @IsInt()
  @Min(1)
  @Type(() => Number) // 确保从 Query Params 接收到的是数字
  category_id?: number; // 按主分类或关联分类过滤

  @IsOptional()
  @IsUUID()
  wholesaler_id?: string; // 零售商想过滤特定批发商的产品 (仅对零售商开放)

  @IsOptional()
  @IsString()
  @IsEnum(['name', 'product_code', 'available_stock', 'price_iva', 'price'])
  sort_by: string = 'name';

  @IsOptional()
  @IsEnum(['asc', 'desc'])
  sort_order: 'asc' | 'desc' = 'asc';

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
