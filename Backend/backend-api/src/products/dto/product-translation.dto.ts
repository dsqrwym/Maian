import { IsNotEmpty, IsOptional, IsString, MaxLength } from 'class-validator';

export class ProductTranslationDto {
  @IsString()
  @IsNotEmpty()
  @MaxLength(10)
  lang_code: string; // 语言代码，例如 'es', 'en'

  @IsString()
  @IsNotEmpty()
  @MaxLength(50)
  name: string;

  @IsOptional()
  @IsString()
  @MaxLength(100)
  title?: string;

  @IsOptional()
  @IsString()
  description?: string;
}
