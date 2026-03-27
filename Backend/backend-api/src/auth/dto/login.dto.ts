import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Transform } from 'class-transformer';
import {
  IsEmail,
  IsOptional,
  IsString,
  IsStrongPassword,
  MaxLength,
  MinLength,
  NotContains,
  ValidateIf,
} from 'class-validator';
import { Trim } from 'src/utils/transform/trim.decorator';
import {
  TagsDeviceName,
  TagsEmail,
  TagsStrongPassword,
  TagsUserAgent,
  TagsUsername,
  TagsWholesalerId,
} from '../../utils/typia/validators/auth.validator';
import { BadRequestException } from '@nestjs/common';
import typia from 'typia';
import { isObject } from '../../utils/is.util';
import { cleanString } from '../../utils/string.util';
import { IRequestBodyValidator } from '@nestia/core/src/options/IRequestBodyValidator';

export class LoginDto {
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
  password: string; // 密码

  @ApiPropertyOptional({
    description:
      'The user\'s email address. Must be a valid email format and cannot belong to the "example.com" domain.',
    example: 'user@example.com',
  })
  // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
  @ValidateIf((o) => !o.username)
  @IsEmail({ host_blacklist: ['example.com'] }) // 验证为邮箱格式，并排除example.com域名
  @MaxLength(100)
  @Trim()
  email?: string; // 邮箱地址

  @ApiPropertyOptional({
    description:
      'The user\'s email address. Must be a valid email format and cannot belong to the "example.com" domain.',
    example: 'user@example.com',
  })
  // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
  @ValidateIf((o) => !o.email)
  @IsString() // 验证为字符串
  @MinLength(3) // 最小长度为3
  @MaxLength(30) // 最大长度为30
  @NotContains('@') // 不能包含 @
  @Trim()
  username?: string; // 用户名

  @ApiPropertyOptional({
    description:
      'Optional wholesaler ID (company code). Used for employees (e.g., WHO001@support). Required if multiple users share the same username across wholesalers.',
    example: 'WHO001',
  })
  @IsOptional()
  @IsString()
  @MinLength(3)
  @MaxLength(20)
  wholesalerId?: string;

  @ApiProperty({
    description:
      'The name of the device used for login. Will be converted to uppercase.',
    example: 'CHROME_BROWSER',
  })
  @IsString()
  @MaxLength(150)
  @Transform(({ value }) => String(value).toUpperCase()) // 将设备名称转换为大写
  deviceName: string; // 登录设备名称

  @ApiProperty({
    description:
      'The user-agent string of the device used for login. Will be converted to uppercase.',
    example: 'MOZILLA/5.0 (WINDOWS NT 10.0; WIN64; X64)',
  })
  @IsString()
  @MaxLength(255)
  @Transform(({ value }) => String(value).toUpperCase()) // 将user-agent转换为大写
  userAgent: string; // 登录设备
}

export interface ILoginDto {
  password: string & TagsStrongPassword; // 密码

  email?: string & TagsEmail; // 邮箱地址

  username?: string & TagsUsername; // 用户名

  wholesalerId?: null | (string & TagsWholesalerId);

  deviceName: string & TagsDeviceName; // 登录设备名称

  userAgent: string & TagsUserAgent; // 登录设备
}

export const validateLogin: IRequestBodyValidator.IAssert<ILoginDto> = {
  type: 'assert',
  assert: (input: unknown) => {
    if (isObject(input)) {
      const obj = input;
      // 自分配（避免额外对象分配）, javascript对象赋值给另一个变量时，只是复制了指针
      if (typeof obj.email === 'string') {
        obj.email = cleanString(obj.email);
      }
      if (typeof obj.username === 'string') {
        obj.username = cleanString(obj.username);
      }
      if (typeof obj.deviceName === 'string') {
        obj.deviceName = obj.deviceName.toUpperCase();
      }
      if (typeof obj.userAgent === 'string') {
        obj.userAgent = obj.userAgent.toUpperCase();
      }
    }
    const typedInput = typia.assertEquals<ILoginDto>(input);

    if (!typedInput.email && !typedInput.username) {
      throw new BadRequestException('Either email or username is required');
    }

    return typedInput;
  },
};
