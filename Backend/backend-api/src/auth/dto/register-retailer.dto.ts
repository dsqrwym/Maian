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
} from 'class-validator';
import { DirectionDto } from './register.direction.dto';
import { Type } from 'class-transformer';
import { Trim } from 'src/utils/transform/trim.decorator';

/**
 * DTO for retailer registration
 * 零售商注册数据传输对象
 */
export class RegisterRetailerDto {
  /**
   * User's email address
   * 用户邮箱地址
   */
  @ApiProperty({
    description: 'The email address for the retailer account',
    example: 'retailer@example.com',
    required: true,
  })
  @IsEmail(
    { host_blacklist: ['example.com'] },
    { message: 'Invalid email format' },
  )
  @MaxLength(100, {
    message: 'Email must be shorter than or equal to 100 characters',
  })
  @IsNotEmpty({ message: 'Email is required' })
  @Trim()
  email: string;

  /**
   * User's password
   * 用户密码
   */
  @ApiProperty({
    description:
      'The password for the account. Must be 6-50 characters long and include at least one uppercase letter, one lowercase letter, and one number.',
    example: 'SecurePass123!',
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
   * Username (optional, must be unique)
   * 用户名（可选，必须唯一）
   */
  @ApiPropertyOptional({
    description: 'The username for the account (optional)',
    example: 'retailer123',
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
  @Trim()
  username?: string;

  /**
   * Store's business address
   * 商店经营地址
   */
  @ApiProperty({
    description: 'The business/store address for the retailer',
    type: () => DirectionDto,
    required: true,
  })
  @Type(() => DirectionDto)
  @ValidateNested({ each: true })
  @IsNotEmpty({ message: 'Address is required' })
  address: DirectionDto;

  @ApiProperty({
    description:
      'Unique identifier for the password reset verification process',
    example: '123e4567-e89b-12d3-a456-426614174000',
  })
  @IsUUID()
  verification_id: string;

  @ApiProperty({
    description: 'JWT token to be used for password reset',
    example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
  })
  @IsString()
  @IsNotEmpty()
  token: string;
}
