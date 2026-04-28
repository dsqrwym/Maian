import type { TagsBCP47Language } from '#/utils/typia/validators/language.validator.js';
import type { TagsNotBlank } from '#/utils/typia/tags/string.tag.js';
import type { tags } from 'typia';
import typia from 'typia';
import { isObject } from '#/utils/is.utils.js';
import { cleanString } from '#/utils/string.util.js';

export interface IProductTranslationDto {
  lang_code: TagsBCP47Language; // 语言代码，例如 'es', 'en'

  name: TagsNotBlank & tags.MaxLength<50> & tags.Example<'Product Name'>;

  title: (string & tags.MaxLength<100> & tags.Example<'Product Name'>) | null;

  description:
    | (string & tags.Example<'Translated detailed description of the product'>)
    | null;
}
export const validateProductTranslationFunction =
  typia.createAssertEquals<IProductTranslationDto>();
export const validateProductTranslationDto = (dto: unknown) => {
  if (isObject(dto)) {
    if (typeof dto.name === 'string') {
      dto.name = cleanString(dto.name);
    }
    if (typeof dto.title === 'string') {
      dto.title = cleanString(dto.title);
    }
  }

  return validateProductTranslationFunction(dto);
};
