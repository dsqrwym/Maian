import {
  IsEnum,
  IsInt,
  IsJSON,
  IsNotEmpty,
  IsNumber,
  IsOptional,
  Max,
  MaxLength,
  Min,
} from 'class-validator';
import { SaleVariant } from 'src/generated/prisma/client';
import { AtLeastOneOf } from '../../common/validators/decorator/at-least-one-field.decorator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class CreateVariantDto {
  // --- 核心销售和定价字段 (variant_products) ---
  @ApiProperty({
    description: 'Type of sales unit for the variant',
    enum: SaleVariant,
  })
  @IsNotEmpty()
  @IsEnum(SaleVariant)
  type_sale: SaleVariant;

  @ApiProperty({
    description: 'Display order; smaller numbers appear first (0-32767)',
    minimum: 0,
    maximum: 32767,
    example: 0,
  })
  @IsNotEmpty()
  @IsNumber()
  @Min(0)
  @Max(32767)
  sort: number; // 显示顺序，数量越小越靠前

  @ApiPropertyOptional({
    description: 'Net price (without VAT/IVA)',
    minimum: 0,
    example: 10.5,
  })
  @IsNumber({ maxDecimalPlaces: 2 })
  @Min(0)
  price?: number; // 不含税价格

  @ApiPropertyOptional({
    description:
      'Gross price (with VAT/IVA). At least one of price or price_iva is required',
    minimum: 0,
    example: 12.71,
  })
  @AtLeastOneOf(['price', 'price_iva']) // 这两种iva至少要有一个不为空
  @IsNumber({ maxDecimalPlaces: 2 })
  @Min(0)
  price_iva?: number; // 含税价格

  @ApiPropertyOptional({
    description:
      'Tax rate applied to this variant (percent). Inherits from product if omitted',
    minimum: 0,
    maximum: 100,
    example: 21,
  })
  @IsOptional()
  @IsNumber({ maxDecimalPlaces: 2 })
  @Min(0)
  @Max(100)
  iva?: number; // 变体适用的税率（不存在则继承自 product.iva）

  @ApiProperty({
    description: 'Unique product code (SKU) for the variant',
    maxLength: 50,
    example: 'SKU-OLIVE-500',
  })
  @IsNotEmpty()
  @MaxLength(50)
  product_code: string; // 变体的编码
  // --- 库存和销售配置字段 (variant_products) ---

  @ApiProperty({
    description: 'Initial available stock for the variant',
    minimum: 0,
    example: 100,
  })
  @IsNotEmpty()
  @IsInt()
  @Min(0)
  available_stock: number; // 初始库存

  @ApiProperty({
    description: 'Conversion factor per sales unit (e.g., 1 box = 24 items)',
    minimum: 1,
    example: 1,
  })
  @IsNotEmpty()
  @IsInt()
  @Min(1)
  sale_unit_qty: number = 1; // 换算因子 (例如：1 箱 = 24 件)

  @ApiProperty({
    description: 'Minimum order quantity in sales units',
    minimum: 1,
    example: 1,
  })
  @IsNotEmpty()
  @IsInt()
  @Min(1)
  min_order_qty: number = 1; // 最小起订量 (以销售单位计)

  @ApiPropertyOptional({
    description: 'Low stock threshold for alerts',
    minimum: 0,
    example: 0,
  })
  @IsOptional()
  @IsInt()
  @Min(0)
  low_stock_threshold?: number = 0; // 低库存预警阈值

  // --- 临时属性 (JSONB) ---
  @ApiPropertyOptional({
    description: 'Temporary attributes in JSON format',
    example: '{"color":"green","size":"500ml"}',
    type: String,
  })
  @IsOptional()
  @IsJSON()
  attributes?: string; // 暂时不用
}
