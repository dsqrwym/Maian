import { IsString, IsUUID } from 'class-validator';
import { ApiPropertyOptional } from '@nestjs/swagger';

export class UploadFileForWholesalerDto {
  @ApiPropertyOptional()
  @IsString()
  @IsUUID()
  wholesalerId?: string;
}
