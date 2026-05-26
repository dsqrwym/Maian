import type { ICreateVariantDto } from './create-product-variant.dto.js';
import type { TagsIntegerString } from '#/utils/typia/tags/string.tag.js';
import type { TagsInt4 } from '#/utils/typia/tags/number.tags.js';
import { isObject } from '#/utils/is.utils.js';
import typia from 'typia';

export interface IUpdateVariantDto extends Partial<
  Omit<ICreateVariantDto, 'available_stock'>
> {
  id: TagsIntegerString;
  available_stock_delta?: TagsInt4;
}
export const validateIUpdateVariantFunction =
  typia.createAssertEquals<IUpdateVariantDto>();
export const validateIUpdateVariant = (input: unknown) => {
  if (isObject(input)) {
    if (typeof input.product_code === 'string') {
      input.product_code = input.product_code.trim();
    }
  }

  return validateIUpdateVariantFunction(input);
};
