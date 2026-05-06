import type { IPaginationQueryDto } from '#/utils/dto/pagination.dto.js';
import type { ProductSelectField } from '../product.enums.js';
import type { TagsLanguage } from '#/utils/typia/validators/language.validator.js';

export interface IProductQueryDto extends IPaginationQueryDto {
  langCode?: TagsLanguage; // 用于指定返回 lang 中的哪个字段

  fields?: ProductSelectField[];
}
