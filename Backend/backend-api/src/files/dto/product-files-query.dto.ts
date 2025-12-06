import { IsNotEmpty, IsString, Matches } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class ProductFilesQueryDto {
  @ApiProperty({
    description: 'Product ID as integer string',
    example: '123',
  })
  @IsString()
  @IsNotEmpty()
  @Matches(/^-?\d+$/, { message: 'must be an integer string' })
  product_id: string;

  @ApiProperty({
    description: 'File ID as integer string',
    example: '456',
  })
  @IsString()
  @IsNotEmpty()
  @Matches(/^-?\d+$/, { message: 'must be an integer string' })
  file_id: string;
}
