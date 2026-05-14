import typia from 'typia';
import type { IRequestBodyValidator } from '#/utils/typia/typia-type.js';
import type { ICreateCartItemDto } from './create-cart-item.dto.js';

export type IUpdateCartItem = Omit<ICreateCartItemDto, 'variant_id'>;
export const validateIUpdateCartItemFunction =
  typia.createAssertEquals<IUpdateCartItem>();
export const validateIUpdateCartItem: IRequestBodyValidator.IAssert<IUpdateCartItem> =
  {
    type: 'assert',
    assert: (input: unknown) => {
      return validateIUpdateCartItemFunction(input);
    },
  };
