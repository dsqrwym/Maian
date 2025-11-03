import { ApiProperty } from '@nestjs/swagger';
import {
  IsArray,
  IsNumber,
  IsOptional,
  IsString,
  Max,
  Min,
  ValidateNested,
} from 'class-validator';
import { CategoryTranslationDto } from './category-translation.dto';
import { Type } from 'class-transformer';
import { AtLeastOneOf } from '../../common/validators/decorator/at-least-one-field.decorator';

export class UpdateCategoryDto {
  @ApiProperty({
    description: 'ID of the category',
    example: 1,
    required: true,
  })
  @IsNumber()
  @AtLeastOneOf(['name', 'iva', 'translations'])
  id: bigint;

  @ApiProperty({
    description: 'Name of the category',
    example: 'Electronics',
    maxLength: 50,
    required: true,
  })
  @IsString()
  name?: string;

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

  @ApiProperty({
    description:
      'List of translation language codes to delete (e.g., remove specific translations)',
    type: [String],
    example: ['fr-FR', 'pt-PT'],
    required: false,
  })
  @IsOptional()
  @IsArray()
  @IsString({ each: true })
  translationsToDelete?: string[];
}
