import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import {
  IsOptional,
  IsString,
  IsUUID,
  MaxLength,
  MinLength,
  IsNumberString,
} from 'class-validator';
import { Trim } from 'src/utils/transform/trim.decorator';

/**
 * DTO for checking category name availability when creating
 */
export class CheckCategoryNameCreateQueryDto {
  @ApiProperty({
    description: 'Category name to check',
    example: 'Electronics',
    maxLength: 50,
  })
  @IsString()
  @MinLength(1)
  @MaxLength(50)
  @Trim()
  name: string;

  @ApiPropertyOptional({
    description:
      'Owner user ID. If omitted, checks public categories (user_id IS NULL).',
    example: '123e4567-e89b-12d3-a456-426614174000',
    required: false,
  })
  @IsOptional()
  @IsUUID()
  userId?: string;
}

/**
 * DTO for checking category name availability when updating
 */
export class CheckCategoryNameUpdateQueryDto {
  @ApiProperty({
    description:
      'ID of the category being updated (to exclude itself in check)',
    example: '1234567890123456',
  })
  @IsNumberString()
  id: string;

  @ApiProperty({
    description: 'Category name to check',
    example: 'Electronics',
    maxLength: 50,
  })
  @IsString()
  @MinLength(1)
  @MaxLength(50)
  @Trim()
  name: string;

  @ApiPropertyOptional({
    description:
      'Owner user ID. If omitted, checks public categories (user_id IS NULL).',
    example: '123e4567-e89b-12d3-a456-426614174000',
    required: false,
  })
  @IsOptional()
  @IsUUID()
  userId?: string;
}
