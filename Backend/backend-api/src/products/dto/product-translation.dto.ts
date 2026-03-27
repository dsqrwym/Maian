import { TagsBCP47Language } from '../../utils/typia/validators/language.validator';
import { TagsNotBlank } from '../../utils/typia/tags/string.tag';
import { tags } from 'typia';
import { isObject } from '../../utils/is.util';
import { cleanString } from 'src/utils/string.util';

export interface IProductTranslationDto {
  lang_code: string & TagsBCP47Language; // 语言代码，例如 'es', 'en'

  name: string &
    TagsNotBlank &
    tags.MaxLength<50> &
    tags.Example<'Product Name'>;

  title?: string & tags.MaxLength<100> & tags.Example<'Product Name'>;

  description?: string &
    tags.Example<'Translated detailed description of the product'>;
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
};
