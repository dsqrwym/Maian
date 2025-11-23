import { PartialType } from '@nestjs/mapped-types';
import { IsNotEmpty, IsString } from 'class-validator';
import { CreateVariantDto } from './create-product-variant.dto';
import { ApiProperty } from '@nestjs/swagger';

export class UpdateVariantDto extends PartialType(CreateVariantDto) {
  @ApiProperty({
    description: 'Variant ID to update',
    example: 'var_123',
  })
  @IsString()
  @IsNotEmpty()
  id: string;
}
