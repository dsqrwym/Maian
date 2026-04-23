import { validateCategoryTranslation } from './category-translation.dto';
import { ICreateCategoryDto } from './create-category.dto';
import { isObject } from '../../utils/is.utils';
import typia from 'typia';
import { cleanString } from '../../utils/string.util';
import { IRequestBodyValidator } from '@nestia/core/src/options/IRequestBodyValidator';
import { TagsVersion } from '../../utils/typia/tags/number.tags';

export interface IUpdateCategoryDto extends Partial<
  Omit<ICreateCategoryDto, 'userId' | 'parentId'>
> {
  translationsToDelete?: string[];
  version: TagsVersion;
}
export const validateUpdateCategory: IRequestBodyValidator.IAssert<IUpdateCategoryDto> =
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

      return typia.assertEquals<IUpdateCategoryDto>(input);
    },
  };
