import type { ICategoryTranslationDto } from './category-translation.dto.js';
import { validateCategoryTranslation } from './category-translation.dto.js';
import type { TagsUuid } from '#/utils/typia/validators/auth.validator.js';
import type { TagsIntegerString } from '#/utils/typia/tags/string.tag.js';
import type { tags } from 'typia';
import typia from 'typia';
import type { TagsCategoryName } from '#/utils/typia/validators/category.validator.js';
import { isObject } from '#/utils/is.utils.js';
import { cleanString } from '#/utils/string.util.js';
import type { TagsIvaString } from '#/utils/typia/validators/product.validator.js';
import type { IRequestBodyValidator } from '#/utils/typia/typia-type.js';
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
export const validateCreateCategoryFunction =
  typia.createAssertEquals<ICreateCategoryDto>();
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

      const body = validateCreateCategoryFunction(input);
      if (body.translations) {
        const codes = body.translations.map((t) => t.lang_code);
        if (new Set(codes).size !== codes.length) {
          throw new BadRequestException('Duplicate lang_code in translations');
        }
      }
      return body;
    },
  };
