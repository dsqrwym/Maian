import type { IProductFilesQueryDto } from '#/files/dto/product-files-query.dto.js';

export interface IVideoStreamQueryDto extends IProductFilesQueryDto {
  playToken: string;
}
