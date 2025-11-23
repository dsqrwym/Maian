import { IsEnum, IsOptional, IsString } from 'class-validator';
import { PaginationQueryDto } from '../../utils/dto/pagination.dto';
import { ApiProperty } from '@nestjs/swagger';
import { ProductSelectField } from '../product.enums';

export class ProductQueryDto extends PaginationQueryDto {
  @ApiProperty({
    description: 'Filter by language code (e.g., en, es)',
    required: false,
  })
  @IsString()
  @IsOptional()
  langCode?: string; // 用于指定返回 lang 中的哪个字段

  // select
  @ApiProperty({
    description: 'Selected fields',
    enum: ProductSelectField,
    required: false,
  })
  @IsEnum(ProductSelectField, {
    each: true,
  })
  @IsOptional()
  fields?: ProductSelectField[];
}
