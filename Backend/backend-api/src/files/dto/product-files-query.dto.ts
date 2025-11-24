import { IsNotEmpty, IsString, Matches } from 'class-validator';

export class ProductFilesQueryDto {
  @IsString()
  @IsNotEmpty()
  @Matches(/^-?\d+$/, { message: 'must be an integer string' })
  product_id: string;

  @IsString()
  @IsNotEmpty()
  @Matches(/^-?\d+$/, { message: 'must be an integer string' })
  file_id: string;
}
