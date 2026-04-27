import type { IPaginationQueryDto } from '#/utils/dto/pagination.dto.js';
import type { ProductSelectField } from '../product.enums.js';

export interface IProductQueryDto extends IPaginationQueryDto {
  langCode?: string; // 用于指定返回 lang 中的哪个字段

  fields?: ProductSelectField[];
}
