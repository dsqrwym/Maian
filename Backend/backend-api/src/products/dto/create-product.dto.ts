import {
  IsInt,
  IsNotEmpty,
  IsNumber,
  IsOptional,
  IsString,
  IsUUID,
  Matches,
  Max,
  MaxLength,
  Min,
  ValidateNested,
} from 'class-validator';
import { Type } from 'class-transformer';
import { CreateVariantDto } from './create-product-variant.dto';
import { ProductTranslationDto } from './product-translation.dto';
import { ProductFileDto } from './product-file.dto';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class CreateProductDto {
  @ApiProperty({
    description: 'ID of the user who owns the product (UUID)',
    format: 'uuid',
  })
  @IsNotEmpty()
  @IsString()
  @IsUUID()
  user_id: string;

  // --- 产品通用信息字段 (products) ---
  @ApiProperty({
    description: 'Primary name of the product in the default language',
    maxLength: 50,
    example: 'Organic Olive Oil',
  })
  @IsNotEmpty()
  @IsString()
  @MaxLength(50)
  name: string; // 主语言名称

  @ApiPropertyOptional({
    description: 'Short title of the product',
    maxLength: 100,
    example: 'Extra Virgin 500ml',
  })
  @IsOptional()
  @IsString()
  @MaxLength(100)
  title?: string;

  @ApiPropertyOptional({
    description: 'Detailed description of the product',
    example: 'Cold-pressed extra virgin olive oil from Spain.',
  })
  @IsOptional()
  @IsString()
  description?: string;

  @ApiProperty({
    description: 'Default VAT/IVA rate for the product (percentage)',
    minimum: 0,
    maximum: 100,
    example: 21,
  })
  @IsNumber({ maxDecimalPlaces: 2 })
  @Min(0)
  @Max(100)
  iva: number; // 产品通用税率

  @ApiProperty({
    description: 'Primary product code (SKU) for the product',
    maxLength: 50,
    example: 'P-OLIVE-500',
  })
  @IsNotEmpty()
  @IsString()
  @MaxLength(50)
  product_code: string; // 主产品编码

  // --- 关键关联字段 (分类) ---
  @ApiProperty({
    description: 'Primary category ID this product belongs to',
    example: '123456',
  })
  @IsNotEmpty()
  @Matches(/^-?\d+$/, { message: 'must be an integer string' })
  @IsString()
  primary_category_id: string; // 必须选择一个主分类 (对应 product_categories.is_primary = TRUE)
  // (后续添加非主分类)

  // --- 核心业务逻辑字段 (变体) ---
  @ApiProperty({
    description: 'List of variants (SKU) for the product. At least 1 required.',
    type: () => [CreateVariantDto],
    minItems: 1,
  })
  @IsNotEmpty()
  @ValidateNested({ each: true })
  @Type(() => CreateVariantDto)
  @Min(1, { message: 'The products need at least 1 variant （SKU）.' })
  variants: CreateVariantDto[];

  // --- 可选关联字段 (翻译和文件) ---
  @ApiPropertyOptional({
    description: 'Localized translations for the product',
    type: () => [ProductTranslationDto],
  })
  @IsOptional()
  @ValidateNested({ each: true })
  @Type(() => ProductTranslationDto)
  translations?: ProductTranslationDto[];

  @ApiPropertyOptional({
    description: 'Related files (e.g., images) with order',
    type: () => [ProductFileDto],
  })
  @IsOptional()
  @ValidateNested({ each: true })
  @Type(() => ProductFileDto)
  files?: ProductFileDto[];
}
