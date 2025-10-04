import { IsEmail, IsOptional, IsString, Matches } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class SendVerificationCodeDto {
  @ApiProperty({
    description:
      'The user\'s email address. Must be a valid email format and cannot belong to the "example.com" domain.',
    example: 'user@example.com',
  })
  @IsEmail({ host_blacklist: ['example.com'] }) // 验证为邮箱格式，并排除example.com域名
  email: string; // 邮箱地址

  @IsOptional()
  @IsString()
  deepLink: string;
}

export class VerifyCodeDto {
  @ApiProperty({
    description: 'The 6-digit numeric verification code.',
    example: '123456',
  })
  @IsString()
  @Matches(/^\d{6}$/, { message: 'Code must be exactly 6 digits' })
  code: string;

  @ApiProperty({
    description:
      'The user\'s email address. Must be a valid email format and cannot belong to the "example.com" domain.',
    example: 'user@example.com',
  })
  @IsEmail({ host_blacklist: ['example.com'] }) // 验证为邮箱格式，并排除example.com域名
  email: string; // 邮箱地址
}

export class VerifyCodeResponseDto {
  @ApiProperty({
    description:
      'Unique identifier for the password reset verification process',
    example: '123e4567-e89b-12d3-a456-426614174000',
  })
  verification_id: string;

  @ApiProperty({
    description: 'JWT token to be used for password reset',
    example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
  })
  token: string;

  @ApiProperty({
    description: 'Expiration timestamp of the verification',
    example: '2023-12-31T23:59:59.999Z',
  })
  expires_at: Date;
}
