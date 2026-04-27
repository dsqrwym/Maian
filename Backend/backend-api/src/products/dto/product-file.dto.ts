import type { TagsIntegerString } from '#/utils/typia/tags/string.tag.js';
import type { TagsSort } from '#/utils/typia/validators/sort.validator.js';

export interface IProductFileDto {
  file_id: TagsIntegerString; // 对应 files.id 要先上传之后拿到id

  sort: TagsSort;
}
