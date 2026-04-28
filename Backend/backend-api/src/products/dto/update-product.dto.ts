import type { IUpdateVariantDto } from './update-product-variant.dto.js';
import { validateIUpdateVariant } from './update-product-variant.dto.js';
import type { IProductTranslationDto } from './product-translation.dto.js';
import { validateProductTranslationDto } from './product-translation.dto.js';
import type { ICreateVariantDto } from './create-product-variant.dto.js';
import { validateICreateVariant } from './create-product-variant.dto.js';
import type { TagsIntegerString } from '#/utils/typia/tags/string.tag.js';
import { isObject } from '#/utils/is.utils.js';
import { cleanString } from '#/utils/string.util.js';
import type { ICreateProductDto } from './create-product.dto.js';
import type { IRequestBodyValidator } from '#/utils/typia/typia-type.js';
import typia from 'typia';
import { BadRequestException } from '@nestjs/common';

type UpdateBase = Partial<
  Omit<ICreateProductDto, 'user_id' | 'variants' | 'translations'>
>;
export interface IUpdateProductDto extends UpdateBase {
  // 覆盖 CreateProductDto 中的 variants，使用 UpdateVariantDto
  updateVariants?: IUpdateVariantDto[];

  createVariants?: ICreateVariantDto[];

  variantsToDelete?: TagsIntegerString[];

  translations?: IProductTranslationDto[];

  translationsToDelete?: string[];
}
export const validateIUpdateProductFunction =
  typia.createAssertEquals<IUpdateProductDto>();
export const validateIUpdateProduct: IRequestBodyValidator.IAssert<IUpdateProductDto> =
  {
    type: 'assert',
    assert: (input: unknown) => {
      if (isObject(input)) {
        if (typeof input.name === 'string')
          input.name = cleanString(input.name);
        if (typeof input.title === 'string')
          input.title = cleanString(input.title);
        if (Array.isArray(input.createVariants)) {
          input.createVariants = input.createVariants.map((it) =>
            validateICreateVariant(it),
          );
        }
        if (Array.isArray(input.updateVariants)) {
          input.updateVariants = input.updateVariants.map((it) =>
            validateIUpdateVariant(it),
          );
        }
        if (Array.isArray(input.translations)) {
          input.translations = input.translations.map(
            validateProductTranslationDto,
          );
        }
      }

      const body = validateIUpdateProductFunction(input);
      if (body.translations) {
        const codes = body.translations.map((t) => t.lang_code);
        if (new Set(codes).size !== codes.length) {
          throw new BadRequestException('Duplicate lang_code in translations');
        }
      }

      return body;
    },
  };
