import type { TagsBCP47Language } from '@/utils/typia/validators/language.validator';
import type { TagsNotBlank } from '@/utils/typia/tags/string.tag';
import type { tags } from 'typia';
import typia from 'typia';
import { isObject } from '@/utils/is.utils';
import { cleanString } from '@/utils/string.util';

export interface ICategoryTranslationDto {
  lang_code: string & TagsBCP47Language;

  name: string & TagsNotBlank & tags.MaxLength<50> & tags.Example<'电子产品'>; // 对应数据库中的 name 字段
}
export const validateCategoryTranslation = (input: unknown) => {
  if (isObject(input)) {
    if (typeof input.name === 'string') input.name = cleanString(input.name);
  }
  return typia.assertEquals<ICategoryTranslationDto>(input);
};
