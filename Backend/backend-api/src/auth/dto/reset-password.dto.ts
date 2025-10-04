import { ApiProperty } from '@nestjs/swagger';
import { IsString, IsStrongPassword, IsUUID, MinLength } from 'class-validator';

export class ResetPasswordDto {
  @IsString()
  @IsUUID()
  verification_id: string;

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
