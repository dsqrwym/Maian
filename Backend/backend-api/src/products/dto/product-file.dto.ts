import { TagsIntegerString } from '../../utils/typia/tags/string.tag';
import { TagsSort } from '../../utils/typia/validators/sort.validator';

export interface IProductFileDto {
  file_id: TagsIntegerString; // 对应 files.id 要先上传之后拿到id

  sort: TagsSort;
}
