import { IsNotEmpty, IsOptional, IsString, MaxLength } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class ProductTranslationDto {
  @ApiProperty({
    description: 'Language code (e.g., es, en)',
    maxLength: 10,
    example: 'en',
  })
  @IsString()
  @IsNotEmpty()
  @MaxLength(10)
  lang_code: string; // 语言代码，例如 'es', 'en'

  @ApiProperty({
    description: 'Translated name of the product',
    maxLength: 50,
  })
  @IsString()
  @IsNotEmpty()
  @MaxLength(50)
  name: string;

  @ApiPropertyOptional({
    description: 'Translated short title of the product',
    maxLength: 100,
  })
  @IsOptional()
  @IsString()
  @MaxLength(100)
  title?: string;

  @ApiPropertyOptional({
    description: 'Translated detailed description of the product',
  })
  @IsOptional()
  @IsString()
  description?: string;
}
