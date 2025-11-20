import { IsInt, IsNotEmpty, Max, Min } from 'class-validator';

export class ProductFileDto {
  @IsNotEmpty()
  @IsInt()
  @Min(1)
  file_id: number; // 对应 files.id 要先上传之后拿到id

  @IsNotEmpty()
  @IsInt()
  @Min(0)
  @Max(32767)
  sort: number;
}
