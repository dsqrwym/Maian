import type { TagsIntegerString } from '#/utils/typia/tags/string.tag.js';
import type { TagsQuantity } from '#/utils/typia/validators/quantity.validator.js';
import typia from 'typia';
import type { IRequestBodyValidator } from '#/utils/typia/typia-type.js';

export interface ICreateCartItemDto {
  variant_id: TagsIntegerString;
  quantity: TagsQuantity;
}

export const validateICreateCartFunction =
  typia.createAssertEquals<ICreateCartItemDto>();
export const validateICreateCartItem: IRequestBodyValidator.IAssert<ICreateCartItemDto> =
  {
    type: 'assert',
    assert: (input: unknown) => {
      return validateICreateCartFunction(input);
    },
  };
