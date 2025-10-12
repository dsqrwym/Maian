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
export class CreateAdminDto {
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

export class VerifyEmployeeEmailJob {
  lang?: string;
  to: string;
  companyName: string;
  position: string;
  link: string;
}

export class ActiveEmployeeWithPasswordEmailJob {
  to: string;
  lang?: string;
  employeeName: string;
  companyName: string;
  temporaryPassword: string;
}
