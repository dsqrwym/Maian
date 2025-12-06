import { ApiProperty } from '@nestjs/swagger';
import { IsString, IsStrongPassword, IsUUID, MinLength } from 'class-validator';

export class ResetPasswordDto {
  @ApiProperty({
    description:
      'Unique identifier for the password reset verification process',
    example: '123e4567-e89b-12d3-a456-426614174000',
  })
  @IsString()
  @IsUUID()
  verification_id: string;

  @ApiProperty({
    description: 'JWT token to be used for password reset',
    example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
  })
  @IsString()
  @IsUUID()
  token: string;

  @ApiProperty({
    description:
      "The user's password. Must be at least 6 characters long and include at least one uppercase letter, one lowercase letter, and one number.",
    example: 'StrongPassword123!',
  })
  @IsString() // 验证为字符串
  @MinLength(6) // 最小长度为6
  @IsStrongPassword({
    minLength: 6, // 最小长度为6
    minLowercase: 1, // 至少包含一个小写字母
    minUppercase: 1, // 至少包含一个大写字母
    minNumbers: 1, // 至少包含一个数字
    minSymbols: 0, // 至少包含一个符号
  }) // 强密码验证，要求至少包含一个数字、一个大写字母和一个小写字母
  newPassword: string; // 密码
}
