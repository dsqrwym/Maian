import { ApiProperty } from '@nestjs/swagger';
import {
  IsEnum,
  IsLatitude,
  IsLongitude,
  IsNumber,
  IsString,
  MaxLength,
} from 'class-validator';
import { AddressType } from '../../../prisma/generated/prisma';
import { Transform } from 'class-transformer';

export class DirectionDto {
  @ApiProperty({
    description: 'Address type',
    enum: AddressType,
    example: 'BILLING',
  })
  @IsEnum(AddressType)
  type: AddressType;

  @ApiProperty({
    description: 'Street address',
    maxLength: 200,
    example: 'Calle Mayor 123, 2ºB',
  })
  @IsString()
  @MaxLength(200)
  direction: string;

  @ApiProperty({
    description: 'City',
    maxLength: 50,
    example: 'MADRID',
  })
  @IsString()
  @MaxLength(50)
  @Transform(({ value }) =>
    // eslint-disable-next-line @typescript-eslint/no-unsafe-return
    typeof value === 'string' ? value.toUpperCase() : value,
  )
  city: string;

  @ApiProperty({
    description: 'Province',
    maxLength: 80,
    example: 'MADRID',
  })
  @IsString()
  @MaxLength(80)
  @Transform(({ value }) =>
    // eslint-disable-next-line @typescript-eslint/no-unsafe-return
    typeof value === 'string' ? value.toUpperCase() : value,
  )
  province: string;

  @ApiProperty({
    description: 'Postal code',
    maxLength: 10,
    example: '28013',
  })
  @IsString()
  @MaxLength(10)
  zip_code: string;

  @ApiProperty({
    description: 'Latitude',
    example: 40.4168,
  })
  @IsNumber()
  @IsLatitude()
  latitude: number;

  @ApiProperty({
    description: 'Longitude',
    example: -3.7038,
  })
  @IsNumber()
  @IsLongitude()
  longitude: number;
}
