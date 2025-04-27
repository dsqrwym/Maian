//DTO -> data transfer object
import {IsString, IsEmail, MinLength, IsOptional, Matches, Min, MaxLength, IsPhoneNumber, IsNumber, Max, IsStrongPassword} from 'class-validator'; // 用于验证类属性的装饰器
import { IsBCP47Language } from 'src/common/validators/decorator/is-bcp47-language.decorator';
import { IsIANA } from 'src/common/validators/decorator/is-iana.decorator';
export class RegisterDto {
    @IsEmail({host_blacklist: ['example.com']}) // 验证为邮箱格式，并排除example.com域名
    email: string; // 邮箱地址

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

    @IsString() // 验证为字符串
    @IsOptional() // 可选属性
    @MinLength(3) // 最小长度为3
    @MaxLength(30) // 最大长度为30
    username: string; // 用户名

    @IsString() // 验证为字符串
    @IsOptional() // 可选属性
    @MaxLength(50) // 最大长度为50
    firstName: string; // 名字

    @IsString() // 验证为字符串
    @IsOptional() // 可选属性
    @MaxLength(60) // 最大长度为60
    lastName: string; // 姓氏

    @IsString() // 验证为字符串
    @IsOptional() // 可选属性
    @MaxLength(15) // 最大长度为15
    @IsPhoneNumber()
    phone: string; // 电话号码

    @IsNumber() // 验证为数字
    @IsOptional() // 可选属性
    @MaxLength(1)
    status: number; // 状态，数字类型

    @IsNumber() // 验证为数字
    @IsOptional() // 可选属性
    @MaxLength(1)
    role: number; // 角色，数字类型




    // 用户设置
    @IsString() // 验证为字符串
    @IsOptional() // 可选属性
    @MaxLength(10) // 最大长度为10
    @IsBCP47Language() // 验证为BCP-47语言代码
    language: string; // 语言设置

    @IsString() // 验证为字符串
    @IsOptional() // 可选属性
    @MaxLength(32) // 最大长度为32
    @IsIANA() // 验证为IANA时区代码
    timezone: string; // 时区设置

}