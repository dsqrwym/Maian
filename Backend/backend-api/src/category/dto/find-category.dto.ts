import { ApiProperty } from '@nestjs/swagger';
import { IsString, IsOptional, IsUUID } from 'class-validator';
import { ToBoolean } from '../../utils/transform-validator';
import { PaginationQueryDto } from '../../utils/dto/pagination.dto';

export class FindCategoryDto extends PaginationQueryDto {
  @ApiProperty({ description: 'Keywords for name search', required: false })
  @IsString()
  @IsOptional()
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
  @IsOptional()
  @IsUUID()
  userId?: string;

  @ApiProperty({
    description: 'Show categories with iva',
    required: false,
    default: false,
  })
  @IsOptional()
  @ToBoolean()
  iva?: boolean;

  @ApiProperty({
    description: 'Show categories with level',
    required: false,
    default: false,
  })
  @IsOptional()
  @ToBoolean()
  level?: boolean;

  @ApiProperty({
    description: 'Show categories with relations: parent and children[]',
    required: false,
    default: false,
  })
  @IsOptional()
  @ToBoolean()
  relations?: boolean;

  @ApiProperty({
    description: 'Show categories with translations',
    required: false,
    default: false,
  })
  @IsOptional()
  @ToBoolean()
  translation?: boolean;
}
