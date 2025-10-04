import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import {
  IsEmail,
  IsNotEmpty,
  IsOptional,
  IsString,
  IsStrongPassword,
  IsUUID,
  MaxLength,
  MinLength,
  NotContains,
  ValidateNested,
  IsPhoneNumber,
} from 'class-validator';
import { Type } from 'class-transformer';
import { DirectionDto } from './register.direction.dto';

/**
 * Enum for Spanish company types
 * 西班牙公司类型枚举
 */
export enum SpanishCompanyType {
  SL = 'S.L.', // Sociedad Limitada（有限责任公司）
  SA = 'S.A.', // Sociedad Anónima（股份有限公司）
  AUTONOMO = 'Autónomo', // 个体户
  COOPERATIVA = 'Cooperativa', // 合作社
  SOCIEDAD_CIVIL = 'Sociedad Civil', // 民事公司
  OTROS = 'Otros',
}

/**
 * DTO for wholesaler registration
 * 批发商注册数据传输对象
 */
export class RegisterWholesalerDto {
  /**
   * User's email address
   * 用户邮箱地址
   */
  @ApiProperty({
    description: 'The email address for the wholesaler account',
    example: 'wholesaler@example.com',
    required: true,
  })
  @IsEmail({}, { message: 'Invalid email format' })
  @MaxLength(100, {
    message: 'Email must be shorter than or equal to 100 characters',
  })
  @IsNotEmpty({ message: 'Email is required' })
  email: string;

  /**
   * User's password
   * 用户密码
   */
  @ApiProperty({
    description:
      'The password for the account. Must be 6-50 characters long and include at least one uppercase letter, one lowercase letter, and one number.',
    example: 'StrongPass123!',
    required: true,
    minLength: 6,
    maxLength: 50,
  })
  @IsString({ message: 'Password must be a string' })
  @IsStrongPassword(
    {
      minLength: 6,
      minLowercase: 1,
      minUppercase: 1,
      minNumbers: 1,
      minSymbols: 0,
    },
    {
      message:
        'Password is too weak. It must be at least 6 characters long and include at least one uppercase letter, one lowercase letter, and one number.',
    },
  )
  password: string;

  /**
   * Username (optional)
   * 用户名（可选）
   */
  @ApiPropertyOptional({
    description: 'The username for the wholesaler account (optional)',
    example: 'wholesaler_001',
    required: false,
    minLength: 3,
    maxLength: 30,
  })
  @IsOptional()
  @IsString({ message: 'Username must be a string' })
  @MinLength(3, { message: 'Username must be at least 3 characters long' })
  @MaxLength(30, {
    message: 'Username must be shorter than or equal to 30 characters',
  })
  @NotContains('@', { message: 'Username cannot contain @ symbol' })
  username?: string;

  /**
   * Company's legal name
   * 公司名称
   */
  @ApiProperty({
    description: 'The registered company name of the wholesaler',
    example: 'Distribuciones Mediterráneo S.L.',
    required: true,
  })
  @IsString({ message: 'Company name must be a string' })
  @IsNotEmpty({ message: 'Company name is required' })
  @MaxLength(100, { message: 'Company name cannot exceed 100 characters' })
  company_name: string;

  /**
   * Company type (Spain)
   * 公司类型（西班牙）
   */
  @ApiProperty({
    description:
      'Type of the company according to Spanish business classification',
    example: SpanishCompanyType.SL,
    required: true,
    enum: SpanishCompanyType,
    enumName: 'CompanyType',
  })
  @IsNotEmpty({ message: 'Company type is required' })
  company_type: SpanishCompanyType;

  /**
   * Telephone number
   * 联系电话
   */
  @ApiProperty({
    description: 'Contact phone number (Spain format supported)',
    example: '+34 612 345 678',
    required: true,
  })
  @IsPhoneNumber('ES', { message: 'Invalid Spanish phone number format' })
  telephone: string;

  /**
   * Business address
   * 经营地址
   */
  @ApiProperty({
    description: 'The business address for the wholesaler',
    type: () => DirectionDto,
    required: true,
  })
  @Type(() => DirectionDto)
  @ValidateNested({ each: true })
  @IsNotEmpty({ message: 'Business address is required' })
  address: DirectionDto;

  /**
   * Verification ID
   * 验证ID
   */
  @ApiProperty({
    description: 'Unique identifier for the verification process',
    example: '123e4567-e89b-12d3-a456-426614174000',
  })
  @IsUUID()
  verification_id: string;

  /**
   * JWT token used for verification
   * 用于验证的JWT令牌
   */
  @ApiProperty({
    description: 'JWT token to verify the registration request',
    example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
  })
  @IsString()
  @IsNotEmpty()
  token: string;
}
