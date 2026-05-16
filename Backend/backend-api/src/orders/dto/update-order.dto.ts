import typia from 'typia';
import type { IRequestBodyValidator } from '#/utils/typia/typia-type.js';
import type { TagsDate } from '#/utils/typia/validators/date.validator.js';

export interface IUpdateOrderDto {
  estimatedDeliveryDate: TagsDate | null;
}

export const validateIUpdateOrderFunction =
  typia.createAssertEquals<IUpdateOrderDto>();
export const validateIUpdateOrderDto: IRequestBodyValidator.IAssert<IUpdateOrderDto> =
  {
    type: 'assert',
    assert: (input: unknown) => {
      return validateIUpdateOrderFunction(input);
    },
  };
