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
  @IsEnum(AddressType)
  type: AddressType;

  @IsString()
  @MaxLength(200)
  direction: string;

  @IsString()
  @MaxLength(50)
  @Transform(({ value }) =>
    // eslint-disable-next-line @typescript-eslint/no-unsafe-return
    typeof value === 'string' ? value.toUpperCase() : value,
  )
  city: string;

  @ApiProperty({ description: 'Province', maxLength: 80 })
  @IsString()
  @MaxLength(80)
  @Transform(({ value }) =>
    // eslint-disable-next-line @typescript-eslint/no-unsafe-return
    typeof value === 'string' ? value.toUpperCase() : value,
  )
  province: string;

  @IsString()
  @MaxLength(10)
  zip_code: string;

  @IsNumber()
  @IsLatitude()
  latitude: number;

  @IsNumber()
  @IsLongitude()
  longitude: number;
}
