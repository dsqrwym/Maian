import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import {
  IsEmail,
  IsNotEmpty,
  IsOptional,
  IsPhoneNumber,
  IsString,
  MaxLength,
  MinLength,
  NotContains,
} from 'class-validator';

/**
 * 批发商系统创建新员工的数据传输对象
 * 包含注册员工账户所需的所有必要信息
 *
 * @class CreateEmployeeDto
 */
export class CreateEmployeeDto {
  /**
   * 员工账户的唯一电子邮箱地址
   * 必须是有效格式且不在黑名单域名中
   *
   * @example 'employee.manager@company.com'
   */
  @ApiProperty({
    description: 'Unique email address for the employee account',
    example: 'employee.manager@company.com',
    required: true,
    maxLength: 100,
    format: 'email',
  })
  @IsEmail(
    { host_blacklist: ['example.com'] },
    { message: 'Please provide a valid email address from a trusted domain' },
  )
  @MaxLength(100, {
    message: 'Email address cannot be longer than 100 characters',
  })
  @IsNotEmpty({ message: 'Email address is required' })
  email: string;

  /**
   * 员工的名字
   *
   * @example 'María'
   */
  @ApiPropertyOptional({
    description: 'Legal first name of the employee',
    example: 'María',
    maxLength: 50,
    required: false,
  })
  @IsString({ message: 'First name must be a text value' })
  @MaxLength(50, {
    message: 'First name cannot exceed 50 characters',
  })
  first_name: string;

  /**
   * 员工的姓氏
   *
   * @example 'García López'
   */
  @ApiPropertyOptional({
    description: 'Legal last name(s) of the employee',
    example: 'García López',
    maxLength: 60,
    required: false,
  })
  @IsOptional()
  @IsString({ message: 'Last name must be a text value' })
  @MaxLength(60, {
    message: 'Last name cannot exceed 60 characters',
  })
  last_name?: string;

  /**
   * 西班牙格式的联系电话
   *
   * @example '+34 600 123 456'
   */
  @ApiPropertyOptional({
    description: 'Contact phone number in Spanish format',
    example: '+34 600 123 456',
    pattern: '^\\+34\\s?[67]\\d{1,2}\\s?\\d{3}\\s?\\d{3}$',
    required: false,
  })
  @IsOptional()
  @IsPhoneNumber('ES', {
    message:
      'Please provide a valid Spanish phone number (e.g., +34 600 123 456)',
  })
  telephone?: string;

  /**
   * 西班牙税务识别号 (CIF/NIF/NIE)
   *
   * @example 'B12345678'
   */
  @ApiPropertyOptional({
    description: 'Spanish tax identification number (CIF/NIF/NIE)',
    example: 'B12345678',
    maxLength: 20,
    pattern: '^[A-Z]?\\d{7,8}[A-Z]?$',
    required: false,
  })
  @IsOptional()
  @IsString({ message: 'Tax ID must be a text value' })
  @MaxLength(20, {
    message: 'Tax ID cannot exceed 20 characters',
  })
  cif?: string;

  /**
   * 系统登录用的唯一用户名
   * 长度必须为3-30个字符，且不能包含'@'符号
   *
   * @example 'mgarciam'
   */
  @ApiPropertyOptional({
    description: 'Unique username for system authentication',
    example: 'mgarciam',
    minLength: 3,
    maxLength: 30,
    pattern: '^[^@]+$',
    required: false,
  })
  @IsOptional()
  @IsString({ message: 'Username must be a text value' })
  @MinLength(3, {
    message: 'Username must be at least 3 characters long',
  })
  @MaxLength(30, {
    message: 'Username cannot exceed 30 characters',
  })
  @NotContains('@', {
    message: 'Username cannot contain the @ symbol',
  })
  username?: string;
}
