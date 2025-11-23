import { IsArray, IsOptional, IsString, ValidateNested } from 'class-validator';
import { Type } from 'class-transformer';
import { UpdateVariantDto } from './update-product-variant.dto';
import { ProductTranslationDto } from './product-translation.dto';
import { CreateProductDto } from './create-product.dto';
import { PartialType } from '@nestjs/mapped-types';
import { OmitType } from '@nestjs/swagger';
import { CreateVariantDto } from './create-product-variant.dto';
import { ApiPropertyOptional } from '@nestjs/swagger';
export class UpdateProductDto extends PartialType(
  OmitType(CreateProductDto, ['user_id', 'variants'] as const),
) {
  // 覆盖 CreateProductDto 中的 variants，使用 UpdateVariantDto
  @ApiPropertyOptional({
    description: 'Existing variants to update (by id)',
    type: () => [UpdateVariantDto],
  })
  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => UpdateVariantDto)
  updateVariants?: UpdateVariantDto[];

  @ApiPropertyOptional({
    description: 'New variants to create and add to the product',
    type: () => [CreateVariantDto],
  })
  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => CreateVariantDto)
  createVariants?: CreateVariantDto[];

  @ApiPropertyOptional({
    description: 'IDs of variants to delete from the product',
    type: () => [String],
    example: ['var_1', 'var_2'],
  })
  @IsOptional()
  @IsArray()
  @IsString({ each: true })
  variantsToDelete?: string[];

  @ApiPropertyOptional({
    description: 'Localized translations to upsert (create or update)',
    type: () => [ProductTranslationDto],
  })
  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => ProductTranslationDto)
  translations?: ProductTranslationDto[];

  @ApiPropertyOptional({
    description: 'Language codes of translations to delete',
    type: () => [String],
    example: ['es', 'en'],
  })
  @IsOptional()
  @IsArray()
  @IsString({ each: true }) // langCode
  translationsToDelete?: string[];
}
