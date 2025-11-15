import { ApiProperty } from '@nestjs/swagger';
import {
  IsString,
  IsOptional,
  IsUUID,
  IsEnum,
  IsNumber,
  Max,
} from 'class-validator';
import { PaginationQueryDto } from '../../utils/dto/pagination.dto';
import { CategorySelectField, CategoryType } from '../category.enums';
import { Trim } from 'src/utils/transform/trim.decorator';

export class FindCategoryDto extends PaginationQueryDto {
  @ApiProperty({ description: 'Keywords for name search', required: false })
  @IsString()
  @IsOptional()
  @Trim()
  search?: string; // 用于 name 和 lang 模糊搜索

  @ApiProperty({
    description: 'Filter by language code (e.g., en, es)',
    required: false,
  })
  @IsString()
  @IsOptional()
  langCode?: string; // 用于指定搜索 lang 中的哪个字段

  @ApiProperty({
    description: 'Filter by user_id',
    required: false,
  })
  @IsUUID()
  @IsOptional()
  userId?: string;

  @ApiProperty({
    description: 'Filter by parent_id',
    required: false,
  })
  @IsString()
  @IsOptional()
  parentId?: string;

  @ApiProperty({
    description: 'Filter by max level',
    required: false,
  })
  @IsOptional()
  @IsNumber()
  @Max(3)
  maxLevel?: number;

  @ApiProperty({
    description: 'Filter by category type',
    enum: CategoryType,
    required: false,
  })
  @IsEnum(CategoryType)
  @IsOptional()
  type?: CategoryType;

  @ApiProperty({
    description: 'Selected fields',
    enum: CategorySelectField,
    required: false,
  })
  @IsEnum(CategorySelectField, {
    each: true,
  })
  @IsOptional()
  fields?: CategorySelectField[];
}
