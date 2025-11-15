import { ApiProperty } from '@nestjs/swagger';
import {
  IsString,
  IsNumber,
  IsOptional,
  IsUUID,
  Min,
  Max,
  IsArray,
  ValidateNested,
} from 'class-validator';
import { CategoryTranslationDto } from './category-translation.dto';
import { Type } from 'class-transformer';
import { Trim } from 'src/utils/transform/trim.decorator';

export class CreateCategoryDto {
  @ApiProperty({
    description: 'User ID who owns this category',
    example: '123e4567-e89b-12d3-a456-426614174000',
    required: false,
  })
  @IsUUID()
  @IsOptional()
  userId?: string;

  @ApiProperty({
    description: 'Name of the category',
    example: 'Electronics',
    maxLength: 50,
    required: true,
  })
  @IsString()
  @Trim()
  name: string;

  @ApiProperty({
    description: 'VAT/IVA rate for the category',
    example: 21.0,
    minimum: 0,
    maximum: 100,
    required: false,
  })
  @IsNumber()
  @Min(0)
  @Max(100)
  @IsOptional()
  iva?: number;

  @ApiProperty({
    description: 'Parent category ID for subcategories',
    example: 1,
    required: false,
  })
  @IsOptional()
  parentId?: string;

  @ApiProperty({
    description: 'Translation data for the category',
    type: [CategoryTranslationDto],
    example: [
      { langCode: 'zh-CH', name: '电子产品' },
      { langCode: 'en-US', name: 'Electronics' },
      { langCode: 'es-ES', name: 'Electrónica' },
    ],
    required: false,
  })
  @IsArray()
  @ValidateNested({ each: true }) // 验证所有
  @Type(() => CategoryTranslationDto)
  translations?: CategoryTranslationDto[];
}
