import {
  ICategoryTranslationDto,
  validateCategoryTranslation,
} from './category-translation.dto';
import { TagsUuid } from '../../utils/typia/validators/auth.validator';
import { TagsIntegerString } from '../../utils/typia/tags/string.tag';
import typia, { tags } from 'typia';
import { TagsCategoryName } from '../../utils/typia/validators/category.validator';
import { isObject } from '../../utils/is.utils';
import { cleanString } from '../../utils/string.util';
import { TagsIvaString } from '../../utils/typia/validators/product.validator';
import { IRequestBodyValidator } from '@nestia/core/src/options/IRequestBodyValidator';

export interface ICreateCategoryDto {
  userId?: TagsUuid;

  name: TagsCategoryName;

  iva?: TagsIvaString;

  parentId?: TagsIntegerString;

  translations?: ICategoryTranslationDto[] &
    tags.Examples<{ langCode: 'es-ES'; name: 'Electrónica' }>;
}
export const validateCreateCategory: IRequestBodyValidator.IAssert<ICreateCategoryDto> =
  {
    type: 'assert',
    assert: (input: unknown) => {
      if (isObject(input)) {
        if (typeof input.name === 'string')
          input.name = cleanString(input.name);
        if (Array.isArray(input.translations)) {
          input.translations = input.translations.map(
            validateCategoryTranslation,
          );
        }
      }
      return typia.assertEquals<ICreateCategoryDto>(input);
    },
  };
