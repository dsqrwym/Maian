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
import { SaleVariant } from '@prisma/client';
import { AtLeastOneOf } from '../../common/validators/decorator/at-least-one-field.decorator';

export class CreateVariantDto {
  // --- 核心销售和定价字段 (variant_products) ---
  @IsNotEmpty()
  @IsEnum(SaleVariant)
  type_sale: SaleVariant;

  @IsNotEmpty()
  @IsNumber()
  @Min(0)
  @Max(32767)
  sort: number; // 显示顺序，数量越小越靠前

  @IsNumber({ maxDecimalPlaces: 2 })
  @Min(0)
  price?: number; // 不含税价格

  @AtLeastOneOf(['price', 'price_iva']) // 这两种iva至少要有一个不为空
  @IsNumber({ maxDecimalPlaces: 2 })
  @Min(0)
  price_iva?: number; // 含税价格

  @IsOptional()
  @IsNumber({ maxDecimalPlaces: 2 })
  @Min(0)
  @Max(100)
  iva?: number; // 变体适用的税率（不存在则继承自 product.iva）

  @IsNotEmpty()
  @MaxLength(50)
  product_code: string; // 变体的编码
  // --- 库存和销售配置字段 (variant_products) ---

  @IsNotEmpty()
  @IsInt()
  @Min(0)
  available_stock: number; // 初始库存

  @IsNotEmpty()
  @IsInt()
  @Min(1)
  sale_unit_qty: number = 1; // 换算因子 (例如：1 箱 = 24 件)

  @IsNotEmpty()
  @IsInt()
  @Min(1)
  min_order_qty: number = 1; // 最小起订量 (以销售单位计)

  @IsOptional()
  @IsInt()
  @Min(0)
  low_stock_threshold?: number = 0; // 低库存预警阈值

  // --- 临时属性 (JSONB) ---
  @IsOptional()
  @IsJSON()
  attributes?: string; // 暂时不用
}
