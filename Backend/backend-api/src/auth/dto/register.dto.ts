import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import {
  IsEmail,
  IsNotEmpty,
  IsOptional,
  IsString,
  MaxLength,
  Validate,
} from 'class-validator'; // 用于验证类属性的装饰器
import { IsBCP47Language } from 'src/common/validators/decorator/is-bcp47-language.decorator';
import { IsIANA } from 'src/common/validators/decorator/is-iana.decorator';

/**
 * DTO for sending normal registration email
 * 发送普通注册邮件的DTO
 */

export class SendNormalRegisterMailDto {
  /**
   * Email address for registration
   * 注册邮箱地址
   */
  @ApiProperty({
    description: 'The email address for registration',
    example: 'user@example.com',
    required: true,
  })
  @IsEmail({}, { message: 'Invalid email format' })
  @MaxLength(100, {
    message: 'Email must be shorter than or equal to 100 characters',
  })
  @IsNotEmpty({ message: 'Email is required' })
  email: string;

  /**
   * Preferred language in BCP-47 format (e.g., en-US, zh-CN)
   * 首选语言（BCP-47格式，例如：en-US, zh-CN）
   */
  @ApiProperty({
    description: 'Preferred language in BCP-47 format (e.g., en-US, zh-CN)',
    example: 'es-ES',
    required: true,
  })
  @IsString({ message: 'Language must be a string' })
  @MaxLength(15, {
    message: 'Language code must be shorter than or equal to 15 characters',
  })
  @Validate(IsBCP47Language, { message: 'Invalid BCP-47 language code' })
  language: string;

  /**
   * Timezone in IANA format (e.g., America/New_York, Asia/Shanghai, Europe/Madrid)
   * 时区（IANA格式，例如：America/New_York, Asia/Shanghai, Europe/Madrid）
   */
  @ApiProperty({
    description:
      'Timezone in IANA format (e.g., America/New_York, Asia/Shanghai, Europe/Madrid)',
    example: 'Europe/Madrid',
    required: true,
  })
  @IsString({ message: 'Timezone must be a string' })
  @MaxLength(50, {
    message: 'Timezone must be shorter than or equal to 50 characters',
  })
  @Validate(IsIANA, { message: 'Invalid IANA timezone' })
  timezone: string;

  /**
   * Deep link for mobile app redirection (optional)
   * 移动应用深度链接（可选）
   */
  @ApiPropertyOptional({
    description: 'Deep link for mobile app redirection',
    example: 'myapp://register/verification',
    required: false,
  })
  @IsOptional()
  @IsString({ message: 'Deep link must be a string' })
  @MaxLength(500, {
    message: 'Deep link must be shorter than or equal to 500 characters',
  })
  deepLink: string;
}
