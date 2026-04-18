import {
  IUpdateVariantDto,
  validateIUpdateVariant,
} from './update-product-variant.dto';
import {
  IProductTranslationDto,
  validateProductTranslationDto,
} from './product-translation.dto';
import {
  ICreateVariantDto,
  validateICreateVariant,
} from './create-product-variant.dto';
import { TagsIntegerString } from '../../utils/typia/tags/string.tag';
import { isObject } from '../../utils/is.utils';
import { cleanString } from '../../utils/string.util';
import { ICreateProductDto } from './create-product.dto';
import { IRequestBodyValidator } from '@nestia/core/src/options/IRequestBodyValidator';
import typia from 'typia';
import { TagsVersion } from '../../utils/typia/tags/number.tags';

type UpdateBase = Omit<ICreateProductDto, 'user_id' | 'variants'>;
export interface IUpdateProductDto extends UpdateBase {
  // 覆盖 CreateProductDto 中的 variants，使用 UpdateVariantDto
  updateVariants?: IUpdateVariantDto[];

  createVariants?: ICreateVariantDto[];

  variantsToDelete?: TagsIntegerString[];

  translations?: IProductTranslationDto[];

  translationsToDelete?: string[];

  version: TagsVersion;
}
export const validateIUpdateProduct: IRequestBodyValidator.IAssert<IUpdateProductDto> =
  {
    type: 'assert',
    assert: (input: unknown) => {
      if (isObject(input)) {
        if (typeof input.name === 'string')
          input.name = cleanString(input.name);
        if (typeof input.title === 'string')
          input.title = cleanString(input.title);
        if (typeof input.iva === 'string') {
          if (Array.isArray(input.createVariants)) {
            input.createVariants = input.createVariants.map((it) =>
              validateICreateVariant(it, input.iva as string),
            );
          }
          if (Array.isArray(input.updateVariants)) {
            input.updateVariants = input.updateVariants.map((it) =>
              validateIUpdateVariant(it, input.iva as string),
            );
          }
        }
        if (Array.isArray(input.translations)) {
          input.translations = input.translations.map(
            validateProductTranslationDto,
          );
        }
      }

      return typia.assertEquals<IUpdateProductDto>(input);
    },
  };
