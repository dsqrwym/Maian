import type { TagsNotBlank } from '../tags/string.tag';
import type { tags } from 'typia';

export type TagsCategoryName = TagsNotBlank &
  tags.MaxLength<50> &
  tags.Example<'Electronics'>;
