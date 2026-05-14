import type { TagsIntegerString } from '#/utils/typia/tags/string.tag.js';

export interface IProductFilesQueryDto {
  product_id: TagsIntegerString;

  file_id: TagsIntegerString;
}
