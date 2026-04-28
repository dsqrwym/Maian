import type { ICreateVariantDto } from './create-product-variant.dto.js';
import type { TagsIntegerString } from '#/utils/typia/tags/string.tag.js';
import { isObject } from '#/utils/is.utils.js';
import typia from 'typia';

export interface IUpdateVariantDto extends Partial<ICreateVariantDto> {
  id: TagsIntegerString;
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
