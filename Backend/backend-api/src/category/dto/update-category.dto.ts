import { validateCategoryTranslation } from './category-translation.dto';
import { ICreateCategoryDto } from './create-category.dto';
import { TagsIntegerString } from '../../utils/typia/tags/string.tag';
import { isObject } from '../../utils/is.util';
import typia from 'typia';
import { BadRequestException } from '@nestjs/common';
import { TagsCategoryName } from '../../utils/typia/validators/category.validator';
import { cleanString } from '../../utils/string.util';
import { IRequestBodyValidator } from '@nestia/core/src/options/IRequestBodyValidator';

export interface IUpdateCategoryDto extends Omit<
  ICreateCategoryDto,
  'userId' | 'parentId' | 'name'
> {
  name?: TagsCategoryName;
  id: TagsIntegerString;
  translationsToDelete?: string[];
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
      const typedBody = typia.assertEquals<IUpdateCategoryDto>(input);
      const { name, iva, translations } = typedBody;
      if (!name && !iva && !translations) {
        throw new BadRequestException(
          "At least one field of ['name', 'iva', 'translations'] is required",
        );
      }
      return typedBody;
    },
  };
