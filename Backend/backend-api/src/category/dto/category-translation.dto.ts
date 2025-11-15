import { ApiProperty } from '@nestjs/swagger';
import { IsString, IsNotEmpty, MaxLength } from 'class-validator';
import { Trim } from 'src/utils/transform/trim.decorator';

export class CategoryTranslationDto {
  @ApiProperty({
    description: 'Language code (e.g., zh-CH, es-ES)',
    example: 'zh-CH',
    maxLength: 10,
    required: true,
  })
  @IsString()
  @IsNotEmpty()
  @MaxLength(10)
  lang_code: string;

  @ApiProperty({
    description: 'Translated name of the category',
    example: '电子产品',
    maxLength: 50,
    required: true,
  })
  @IsString()
  @IsNotEmpty()
  @MaxLength(50)
  @Trim()
  name: string; // 对应数据库中的 name 字段
}
