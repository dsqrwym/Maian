import {
  IsInt,
  IsNotEmpty,
  IsString,
  Matches,
  Max,
  Min,
} from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';
export class ProductFileDto {
  @ApiProperty({
    description: 'ID of the uploaded file associated with this product',
    example: '12345',
  })
  @IsNotEmpty()
  @Matches(/^-?\d+$/, { message: 'must be an integer string' })
  @IsString()
  file_id: string; // 对应 files.id 要先上传之后拿到id

  @ApiProperty({
    description: 'Sorting order for displaying files (0-32767)',
    minimum: 0,
    maximum: 32767,
    example: 0,
  })
  @IsNotEmpty()
  @IsInt()
  @Min(0)
  @Max(32767)
  sort: number;
}
