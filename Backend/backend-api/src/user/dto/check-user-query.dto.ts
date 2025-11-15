import {
  IsEmail,
  IsNotEmpty,
  IsOptional,
  IsString,
  MaxLength,
  MinLength,
  NotContains,
} from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { ToBoolean } from '../../utils/transform/to-boolean.decorator';

/**
 * DTO for checking email availability
 * 检查邮箱可用性的数据传输对象
 */
export class CheckUserEmailQueryDto {
  @ApiPropertyOptional({
    description:
      'The user\'s email address. Must be a valid email format and cannot belong to the "example.com" domain.',
    example: 'user@example.com',
  })
  @IsEmail({ host_blacklist: ['example.com'] }) // 验证为邮箱格式，并排除example.com域名
  @MaxLength(100)
  email: string; // 邮箱地址
}

/**
 * DTO for checking username availability
 * 检查用户名可用性的数据传输对象
 */
export class CheckUserUsernameQueryDto {
  @ApiProperty({
    description:
      'The username to check for availability / 需要检查可用性的用户名',
    example: 'retailer123',
    required: true,
    minLength: 3,
    maxLength: 30,
    pattern: '^[a-zA-Z0-9_\\.-]+$',
  })
  @IsString({ message: 'Username must be a string / 用户名必须是字符串' })
  @MinLength(3, {
    message:
      'Username must be at least 3 characters long / 用户名长度不能少于3个字符',
  })
  @MaxLength(30, {
    message:
      'Username must be shorter than or equal to 30 characters / 用户名长度不能超过30个字符',
  })
  @NotContains('@', { message: 'Username cannot contain @ symbol' })
  username: string;

  @ApiPropertyOptional({
    description:
      'Optional wholesaler ID (company code). Used for employees (e.g., WHO001@support).',
    example: 'WHO001',
  })
  @IsOptional()
  @IsString()
  @IsNotEmpty()
  @MaxLength(20)
  wholesalerId?: string = undefined;

  @ApiProperty({
    description:
      'Whether this check is for admin purposes. If true, only users with ADMIN ROLE will be considered as "used" / 是否为管理员检查。如果为true，则只有ADMIN ROLE的用户会被视为"已使用"',
    example: false,
    default: false,
    required: false,
  })
  @ToBoolean()
  @IsOptional()
  isAdmin?: boolean = false;
}
