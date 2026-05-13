import type { TagsUInt4 } from '#/utils/typia/tags/number.tags.js';
import type { tags } from 'typia';

export type TagsQuantity = TagsUInt4 & tags.Minimum<1> & tags.Maximum<1000000>;
