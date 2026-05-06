import type { tags } from 'typia';
import type { TagsBCP47, TagsIANA, TagsNotBlank } from '../tags/string.tag.js';

/**
 * Preferred language in BCP-47 format (e.g., en-US, zh-CN)
 * 首选语言（BCP-47格式，例如：en-US, zh-CN）
 */
export type TagsBCP47Language = TagsNotBlank &
  tags.MaxLength<15> &
  TagsBCP47 &
  tags.Example<'es-ES'>;

/**
 * 用于搜索相关语言的简易，不修改数据所以只需要最少检验
 */
export type TagsLanguage = TagsNotBlank &
  tags.MaxLength<15> &
  tags.Example<'es-ES'>;

/**
 * Timezone in IANA format (e.g., America/New_York, Asia/Shanghai, Europe/Madrid)
 * 时区（IANA格式，例如：America/New_York, Asia/Shanghai, Europe/Madrid）
 */
export type TagsIANATimezone = TagsNotBlank &
  tags.MaxLength<50> &
  TagsIANA &
  tags.Example<'Europe/Madrid'>;
