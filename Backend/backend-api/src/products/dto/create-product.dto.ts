import {
  IsInt,
  IsNotEmpty,
  IsNumber,
  IsOptional,
  IsString,
  IsUUID,
  Max,
  MaxLength,
  Min,
  ValidateNested,
} from 'class-validator';
import { Type } from 'class-transformer';
import { CreateVariantDto } from './create-product-variant.dto';
import { ProductTranslationDto } from './product-translation.dto';
import { ProductFileDto } from './product-file.dto';

export class CreateProductDto {
  @IsUUID()
  user_id: string;

  // --- 产品通用信息字段 (products) ---
  @IsNotEmpty()
  @IsString()
  @MaxLength(50)
  name: string; // 主语言名称

  @IsOptional()
  @IsString()
  @MaxLength(100)
  title?: string;

  @IsOptional()
  @IsString()
  description?: string;

  @IsNumber({ maxDecimalPlaces: 2 })
  @Min(0)
  @Max(100)
  iva: number; // 产品通用税率

  @IsNotEmpty()
  @IsString()
  @MaxLength(50)
  product_code: string; // 主产品编码

  // --- 关键关联字段 (分类) ---
  @IsNotEmpty()
  @IsInt()
  @Min(1)
  primary_category_id: number; // 必须选择一个主分类 (对应 product_categories.is_primary = TRUE)
  // (后续添加非主分类)

  // --- 核心业务逻辑字段 (变体) ---
  @IsNotEmpty()
  @ValidateNested({ each: true })
  @Type(() => CreateVariantDto)
  @Min(1, { message: 'The products need at least 1 variant （SKU）.' })
  variants: CreateVariantDto[];

  // --- 可选关联字段 (翻译和文件) ---
  @IsOptional()
  @ValidateNested({ each: true })
  @Type(() => ProductTranslationDto)
  translations?: ProductTranslationDto[];

  @IsOptional()
  @ValidateNested({ each: true })
  @Type(() => ProductFileDto)
  files?: ProductFileDto[];
}
