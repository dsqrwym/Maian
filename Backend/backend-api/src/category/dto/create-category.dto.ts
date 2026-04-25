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
import { BadRequestException } from '@nestjs/common';

export interface ICreateCategoryDto {
  userId: TagsUuid | null;

  name: TagsCategoryName;

  iva: TagsIvaString | null;

  parentId: TagsIntegerString | null;

  translations:
    | (ICategoryTranslationDto[] &
        tags.Examples<{ langCode: 'es-ES'; name: 'Electrónica' }>)
    | null;
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

      const body = typia.assertEquals<ICreateCategoryDto>(input);
      if (body.translations) {
        const codes = body.translations.map((t) => t.lang_code);
        if (new Set(codes).size !== codes.length) {
          throw new BadRequestException('Duplicate lang_code in translations');
        }
      }
      return body;
    },
  };
