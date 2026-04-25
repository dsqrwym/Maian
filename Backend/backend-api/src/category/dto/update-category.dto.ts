import { validateCategoryTranslation } from './category-translation.dto';
import { ICreateCategoryDto } from './create-category.dto';
import { isObject } from '@/utils/is.utils';
import typia from 'typia';
import { cleanString } from '@/utils/string.util';
import { IRequestBodyValidator } from '@nestia/core/src/options/IRequestBodyValidator';
import { TagsVersion } from '@/utils/typia/tags/number.tags';
import { BadRequestException } from '@nestjs/common';

export interface IUpdateCategoryDto extends Partial<
  Omit<ICreateCategoryDto, 'userId' | 'parentId'>
> {
  translationsToDelete?: string[];
  /**
   * The current version of the category. Required for optimistic concurrency control.
   * This value is obtained from `GET /:id/update`.
   * If the version does not match the latest one on the server, a 409 Conflict is returned.
   */
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

      const body = typia.assertEquals<IUpdateCategoryDto>(input);
      if (body.translations) {
        const codes = body.translations.map((t) => t.lang_code);
        if (new Set(codes).size !== codes.length) {
          throw new BadRequestException('Duplicate lang_code in translations');
        }
      }

      return body;
    },
  };
