import type { TagsUInt2 } from '../tags/number.tags.js';
import type { tags } from 'typia';
import type { OrderByEnum } from '#/common/enums/sort.enum.js';

/**
 * 用于排序
 */
export type TagsSort = TagsUInt2 & tags.Example<0>;
export type TagsSortOrder = OrderByEnum & tags.Example<'asc'>;
