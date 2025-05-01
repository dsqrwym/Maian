//DTO -> data transfer object
import { ApiProperty } from '@nestjs/swagger';
import { Transform, Type } from 'class-transformer';
import { IsString, IsEmail, MinLength, IsOptional, MaxLength, IsPhoneNumber, IsNumber, IsStrongPassword, Validator, Validate, Max, ValidateNested, IsIn, Matches, IsLatitude, IsLongitude, Min } from 'class-validator'; // 用于验证类属性的装饰器
import { IsBCP47Language } from 'src/common/validators/decorator/is-bcp47-language.decorator';
import { IsIANA } from 'src/common/validators/decorator/is-iana.decorator';

class ProfileDto {
    // 批发商和零售商的个人资料设置
    @ApiProperty({ description: 'Company name', example: 'Distribuciones SL' })
    @IsString()
    @MaxLength(100)
    @Transform(({ value }) => value?.toUpperCase())
    @IsOptional()
    companyName: string;

    @ApiProperty({ description: 'Company type (e.g. 0=SA, 1=AUTONOMO, 2=SL, 3=SLNE, 4=SC, 5=CB, 6=COOP, 7=ASOCIACION)', example: 1 })
    /*
    SpanishCompanyType {
        SA = 0,       // Sociedad Anónima
        AUTONOMO = 1, // Autónomo (个体)
        SL = 2,       // Sociedad Limitada
        SLNE = 3,     // Nueva Empresa
        SC = 4,       // Sociedad Civil
        CB = 5,       // Comunidad de Bienes
        COOP = 6,     // Cooperativa
        ASOCIACION = 7, // Asociación / Fundación
    }*/
    @IsNumber()
    @Min(0)
    @Max(7)
    @IsOptional()
    companyType: number;

    @IsString()
    @IsOptional()
    licence: string // 储存营业执照的文件id

    // 批发商的个人资料设置
    @ApiProperty({ description: 'Company description', required: false })
    @IsString()
    @IsOptional()
    companyDescription: string;


}

class DirectionDto {
    @ApiProperty({ description: 'Direction type: 0=Shipping, 1=Billing, 2=Company', example: 0 })
    @IsNumber()
    @IsIn([0, 1, 2])
    type: number;

    @ApiProperty({ description: 'Street address', maxLength: 200, example: 'Calle de Example, 123' })
    @IsString()
    @MaxLength(200)
    direction: string;

    @ApiProperty({ description: 'City', maxLength: 50, example: 'Madrid' })
    @IsString()
    @Transform(({ value }) => value?.toUpperCase())
    @MaxLength(50)
    city: string;

    @ApiProperty({ description: 'Province', maxLength: 80, example: 'Madrid' })
    @IsString()
    @Transform(({ value }) => value?.toUpperCase())
    @MaxLength(80)
    province: string;

    @ApiProperty({ description: 'ZIP code', maxLength: 10, example: '28001' })
    @IsString()
    @MaxLength(10)
    zip_code: string;

    @ApiProperty({ description: 'Latitude', example: 40.4168 })
    @IsNumber()
    @IsLatitude()
    latitude: number;

    @ApiProperty({ description: 'Longitude', example: -3.7038 })
    @IsNumber()
    @IsLongitude()
    longitude: number;
}

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
    @MinLength(3) // 最小长度为3
    @MaxLength(30) // 最大长度为30
    @IsOptional() // 可选属性
    username: string; // 用户名

    @ApiProperty({
        description: 'First name',
        example: 'Juan',
        required: false,
        maxLength: 50
    })
    @IsString() // 验证为字符串
    @Transform(({ value }) => value?.toUpperCase())
    @MaxLength(50) // 最大长度为50
    @IsOptional() // 可选属性
    firstName: string; // 名字

    @ApiProperty({
        description: 'Last name',
        example: 'Smith',
        required: false,
        maxLength: 60
    })
    @IsString() // 验证为字符串
    @Transform(({ value }) => value?.toUpperCase())
    @MaxLength(60) // 最大长度为60
    @IsOptional() // 可选属性
    lastName: string; // 姓氏


    @IsString() // 验证为字符串
    @MaxLength(9) // 最大长度为9
    @Transform(({ value }) => value?.toUpperCase())
    @Matches(/(^[A-Z]\d{8}$)|(^[XYZ]\d{7}[A-Z]$)|(^\d{8}[A-Z]$)/, { message: 'cif,nie,nif' }) // 匹配西班牙身份证号码格式
    @IsOptional() // 可选属性
    cif: string; // 身份证号码

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
        description: 'Business type (0=Wholesaler ,1=Retailer)',
        example: 1,
        required: false,
        maximum: 9
    })
    @IsNumber() // 验证为数字
    @IsOptional() // 可选属性
    @IsIn([0, 1]) // 只能是0或1
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

    @ApiProperty({
        description: 'List of user addresses (shipping, billing, company)',
        type: [DirectionDto],
        required: false
    })
    @ValidateNested({ each: true })
    @Type(() => DirectionDto)
    @IsOptional()
    address: DirectionDto[];

    @ApiProperty({
        description: 'Profile information for retailer or wholesaler',
        type: ProfileDto,
        required: false
    })
    @ValidateNested()
    @Type(() => ProfileDto)
    @IsOptional()
    profile: ProfileDto;// 个人资料设置，使用嵌套对象进行验证 分为零售商和批发商
}