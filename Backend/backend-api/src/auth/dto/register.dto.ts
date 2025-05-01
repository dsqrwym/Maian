//DTO -> data transfer object
import { ApiProperty } from '@nestjs/swagger';
import { IsString, IsEmail, MinLength, IsOptional, MaxLength, IsPhoneNumber, IsNumber, IsStrongPassword, Validator, Validate, Max } from 'class-validator'; // 用于验证类属性的装饰器
import { IsBCP47Language } from 'src/common/validators/decorator/is-bcp47-language.decorator';
import { IsIANA } from 'src/common/validators/decorator/is-iana.decorator';
export class RegisterDto {
    @ApiProperty({
        description: 'Valid email address',
        example: 'user@domain.com',
        required: true
    })
    @IsEmail({ host_blacklist: ['example.com'] }) // 验证为邮箱格式，并排除example.com域名
    email: string; // 邮箱地址

    @ApiProperty({
        description: 'Strong password (6+ chars with uppercase, lowercase and number)',
        example: 'SecurePass123',
        required: true
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
        description: 'Username (3-30 characters)',
        example: 'YUxz123',
        required: false
    })
    @IsString() // 验证为字符串
    @IsOptional() // 可选属性
    @MinLength(3) // 最小长度为3
    @MaxLength(30) // 最大长度为30
    username: string; // 用户名

    @ApiProperty({
        description: 'First name',
        example: 'Juan',
        required: false,
        maxLength: 50
    })
    @IsString() // 验证为字符串
    @IsOptional() // 可选属性
    @MaxLength(50) // 最大长度为50
    firstName: string; // 名字

    @ApiProperty({
        description: 'Last name',
        example: 'Smith',
        required: false,
        maxLength: 60
    })
    @IsString() // 验证为字符串
    @IsOptional() // 可选属性
    @MaxLength(60) // 最大长度为60
    lastName: string; // 姓氏

    @ApiProperty({
        description: 'Phone number in E.164 format',
        example: '+1234567890',
        required: false
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
        maximum: 9
    })
    @IsNumber() // 验证为数字
    @IsOptional() // 可选属性
    @Max(10)
    status: number; // 状态，数字类型


    @ApiProperty({
        description: 'User role',
        example: 1,
        required: false,
        maximum: 9
    })
    @IsNumber() // 验证为数字
    @IsOptional() // 可选属性
    @Max(10)
    role: number; // 角色，数字类型




    // 用户设置
    @ApiProperty({
        description: 'BCP-47 language code',
        example: 'es-ES',
        required: false
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
        required: false
    })
    @IsString() // 验证为字符串
    @IsOptional() // 可选属性
    @MaxLength(32) // 最大长度为32
    @Validate(IsIANA()) // 验证为IANA时区代码
    timezone: string; // 时区设置

}