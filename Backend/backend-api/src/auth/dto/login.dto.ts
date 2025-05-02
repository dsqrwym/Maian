import { IsEmail, IsString, MinLength, IsStrongPassword, IsOptional, MaxLength, ValidateIf } from "class-validator";
export class LoginDto {

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

    @ValidateIf((o) => !o.username)
    @IsEmail({ host_blacklist: ['example.com'] }) // 验证为邮箱格式，并排除example.com域名
    email: string; // 邮箱地址
    
    @ValidateIf((o) => !o.email)
    @IsString() // 验证为字符串
    @MinLength(3) // 最小长度为3
    @MaxLength(30) // 最大长度为30
    username: string; // 用户名

    @IsOptional()
    @IsString()
    @MaxLength(150)
    deciceName: string // 登陆设备名称

    @IsOptional()
    @IsString()
    @MaxLength(39)
    ipAddress: string // 登陆地址

    @IsOptional()
    @IsString()
    userAgent: string //  登录设备
}