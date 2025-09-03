//DTO -> data transfer object
import { ApiExtraModels, ApiProperty } from '@nestjs/swagger';
import { Transform, Type } from 'class-transformer';
import {
  IsString,
  IsEmail,
  MinLength,
  IsOptional,
  MaxLength,
  IsPhoneNumber,
  IsStrongPassword,
  Validate,
  ValidateNested,
  Matches,
  IsArray,
  IsEnum,
  NotContains,
} from 'class-validator'; // 用于验证类属性的装饰器
import { IsBCP47Language } from 'src/common/validators/decorator/is-bcp47-language.decorator';
import { IsIANA } from 'src/common/validators/decorator/is-iana.decorator';
import { UserRole, UserStatus } from '../../../prisma/generated/prisma';
import { RegisterProfileDto } from './register.profile.dto';
import { DirectionDto } from './register.direction.dto';

@ApiExtraModels(RegisterProfileDto, DirectionDto)
export class RegisterDto {
  @ApiProperty({
    description: 'Valid email address',
    example: 'user@domain.com',
  })
  @IsEmail({ host_blacklist: ['example.com'] })
  email: string;

  @ApiProperty({
    description:
      'Strong password (6+ chars with uppercase, lowercase and number)',
    example: 'SecurePass123',
    required: true,
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

  @ApiProperty({
    description: 'Username',
    minLength: 3,
    maxLength: 30,
    example: 'username',
    required: false,
  })
  @IsString() // 验证为字符串
  @MinLength(3) // 最小长度为3
  @MaxLength(30) // 最大长度为30
  @NotContains('@')
  @IsOptional() // 可选属性
  username: string; // 用户名

  @ApiProperty({
    description: 'First name',
    maxLength: 50,
    required: false,
  })
  @IsString() // 验证为字符串
  @Transform(({ value }) =>
    // eslint-disable-next-line @typescript-eslint/no-unsafe-return
    typeof value === 'string' ? value.toUpperCase() : value,
  )
  @MaxLength(50) // 最大长度为50
  @IsOptional() // 可选属性
  firstName: string; // 名字

  @ApiProperty({
    description: 'Last name',
    example: 'Smith',
    required: false,
    maxLength: 60,
  })
  @IsString() // 验证为字符串
  @Transform(({ value }) =>
    // eslint-disable-next-line @typescript-eslint/no-unsafe-return
    typeof value === 'string' ? value.toUpperCase() : value,
  )
  @MaxLength(60) // 最大长度为60
  @IsOptional() // 可选属性
  lastName: string; // 姓氏

  @IsString() // 验证为字符串
  @MaxLength(9) // 最大长度为9
  @Transform(({ value }) =>
    // eslint-disable-next-line @typescript-eslint/no-unsafe-return
    typeof value === 'string' ? value.toUpperCase() : value,
  )
  @Matches(/(^[A-Z]\d{8}$)|(^[XYZ]\d{7}[A-Z]$)|(^\d{8}[A-Z]$)/, {
    message: 'cif,nie,nif',
  }) // 匹配西班牙身份证号码格式
  @IsOptional() // 可选属性
  cif: string; // 身份证号码

  @ApiProperty({
    description: 'Phone number in E.164 format',
    example: '+1234567890',
    required: false,
  })
  @IsString() // 验证为字符串
  @IsOptional() // 可选属性
  @MaxLength(15) // 最大长度为15
  @IsPhoneNumber()
  phone: string; // 电话号码

  @ApiProperty({
    description: 'User status',
    example: 0,
    required: false,
    maximum: 9,
  })
  @IsEnum(UserStatus)
  @IsOptional() // 可选属性
  status: UserStatus; // 状态，数字类型

  @IsOptional() // 可选属性
  @IsEnum(UserRole)
  role: UserRole; // 角色，数字类型

  // 用户设置
  @ApiProperty({
    description: 'BCP-47 language code',
    example: 'es-ES',
    required: false,
  })
  @IsString() // 验证为字符串
  @IsOptional() // 可选属性
  @MaxLength(15) // 最大长度为15
  //@IsBCP47Language() // 验证为BCP-47语言代码
  @Validate(IsBCP47Language())
  language: string; // 语言设置

  @ApiProperty({
    description: 'IANA timezone code',
    example: 'Europe/Madrid',
    required: false,
  })
  @IsString() // 验证为字符串
  @IsOptional() // 可选属性
  @MaxLength(32) // 最大长度为32
  @Validate(IsIANA()) // 验证为IANA时区代码
  timezone: string; // 时区设置

  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => DirectionDto)
  @IsOptional()
  address: DirectionDto[];

  @ApiProperty({
    description: 'Profile information for retailer or wholesaler',
    type: RegisterProfileDto,
    required: false,
  })
  @ValidateNested()
  @Type(() => RegisterProfileDto)
  @IsOptional()
  profile: RegisterProfileDto; // 个人资料设置，使用嵌套对象进行验证 分为零售商和批发商
}
