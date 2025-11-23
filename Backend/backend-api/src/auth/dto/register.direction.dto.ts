import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import {
  IsEnum,
  IsLatitude,
  IsLongitude,
  IsNumber,
  IsOptional,
  IsString,
  Max,
  MaxLength,
  Min,
} from 'class-validator';
import { AddressType } from 'src/generated/prisma/client';
import { Type } from 'class-transformer';
import { Trim } from 'src/utils/transform/trim.decorator';

/**
 * DTO for address information
 * 地址信息数据传输对象
 */
export class DirectionDto {
  /**
   * Type of the address (e.g., STORE, DELIVERY)
   * 地址类型（例如：营业地址、收货地址）
   */
  @ApiProperty({
    description: 'Type of the address (e.g., STORE, DELIVERY)',
    enum: AddressType,
    default: AddressType.STORE,
    example: 'STORE',
  })
  @IsEnum(AddressType)
  @IsOptional()
  type: AddressType = AddressType.STORE;

  /**
   * Street address including building and apartment number
   * 街道地址，包括门牌号和公寓号
   */
  @ApiProperty({
    description: 'Street address including building and apartment number',
    maxLength: 200,
    example: '123 Main St, Apt 4B',
  })
  @IsString()
  @MaxLength(200)
  @Trim()
  street: string;

  /**
   * City ID reference
   * 城市ID
   */
  @ApiProperty({
    description: 'ID of the city',
    example: 1,
  })
  @IsNumber()
  @Min(1)
  @Type(() => Number)
  city: number;

  /**
   * Province/State ID reference
   * 省份/州ID
   */
  @ApiProperty({
    description: 'ID of the province/state',
    example: 2,
  })
  @IsNumber()
  @Min(1)
  @Type(() => Number)
  province: number;

  /**
   * Country ID reference, ISO 3166-1 numeric code
   * 国家ISO 3166-1 numeric
   */
  @ApiProperty({
    description: 'ID of the country, ISO 3166-1 numeric code',
    example: 724,
  })
  @IsNumber()
  @Min(1)
  @Max(999)
  @Type(() => Number)
  country: number;

  /**
   * Postal/ZIP code
   * 邮政编码
   */
  @ApiProperty({
    description: 'Postal/ZIP code',
    maxLength: 10,
    example: '10001',
  })
  @IsString()
  @MaxLength(10)
  zipCode: string;

  /**
   * Latitude coordinate (optional)
   * 纬度坐标（可选）
   */
  @ApiPropertyOptional({
    description: 'Latitude coordinate',
    example: 40.7128,
    required: false,
  })
  @IsOptional()
  @IsLatitude()
  @Type(() => Number)
  latitude?: number;

  /**
   * Longitude coordinate (optional)
   * 经度坐标（可选）
   */
  @ApiPropertyOptional({
    description: 'Longitude coordinate',
    example: -74.006,
    required: false,
  })
  @IsOptional()
  @IsLongitude()
  @Type(() => Number)
  longitude?: number;
}
