import { TagsBCP47Language } from '@/utils/typia/validators/language.validator';
import { TagsNotBlank } from '@/utils/typia/tags/string.tag';
import typia, { tags } from 'typia';
import { isObject } from '@/utils/is.utils';
import { cleanString } from 'src/utils/string.util';

export interface IProductTranslationDto {
  lang_code: TagsBCP47Language; // 语言代码，例如 'es', 'en'

  name: TagsNotBlank & tags.MaxLength<50> & tags.Example<'Product Name'>;

  title: (string & tags.MaxLength<100> & tags.Example<'Product Name'>) | null;

  description:
    | (string & tags.Example<'Translated detailed description of the product'>)
    | null;
}
export const validateProductTranslationDto = (dto: unknown) => {
  if (isObject(dto)) {
    if (typeof dto.name === 'string') {
      dto.name = cleanString(dto.name);
    }
    if (typeof dto.title === 'string') {
      dto.title = cleanString(dto.title);
    }
  }

  return typia.assertEquals<IProductTranslationDto>(dto);
};
