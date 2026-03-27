import { TagsNotBlank } from '../tags/string.tag';
import { tags } from 'typia';

export type TagsCategoryName = TagsNotBlank &
  tags.MaxLength<50> &
  tags.Example<'Electronics'>;
