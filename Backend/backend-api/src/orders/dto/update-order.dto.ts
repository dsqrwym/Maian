import typia from 'typia';
import type { IRequestBodyValidator } from '#/utils/typia/typia-type.js';

export interface IUpdateOrderDto {
  estimatedDeliveryDate: (string & typia.tags.Format<'date'>) | null;
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
