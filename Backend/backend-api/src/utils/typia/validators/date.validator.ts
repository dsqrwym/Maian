import type { tags } from 'typia';

export type TagsDate = string &
  tags.Format<'date'> &
  tags.Example<'2026-01-01'>;
