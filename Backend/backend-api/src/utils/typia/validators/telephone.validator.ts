import type { TagsNotBlank } from '../tags/string.tag';
import type { tags } from 'typia';

/**
 * 基础电话号码
 * 最大长度25个字符
 * 应该通过验证函数验证因为typia没有telephone类型
 */
export type TagsBasicTelephone = TagsNotBlank &
  tags.MaxLength<25> &
  tags.Example<'+34 679 876 667'>;
