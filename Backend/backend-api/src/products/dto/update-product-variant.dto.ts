import { PartialType } from '@nestjs/mapped-types';
import { IsNotEmpty, IsString, Matches } from 'class-validator';
import { CreateVariantDto } from './create-product-variant.dto';
import { ApiProperty } from '@nestjs/swagger';

export class UpdateVariantDto extends PartialType(CreateVariantDto) {
  @ApiProperty({
    description: 'Variant ID to update',
    example: '123',
  })
  @IsString()
  @IsNotEmpty()
  @Matches(/^-?\d+$/, { message: 'must be an integer string' })
  id: string;
}
