import type { TagsUuid } from '#/utils/typia/validators/auth.validator.js';
import typia from 'typia';
import type { IRequestBodyValidator } from '#/utils/typia/typia-type.js';

export interface ICreateOrderDto {
  wholesalerId: TagsUuid;
}
export const validateCreateOrderFunction =
  typia.createAssertEquals<ICreateOrderDto>();
export const validateCreateOrder: IRequestBodyValidator.IAssert<ICreateOrderDto> =
  {
    type: 'assert',
    assert: (input: unknown) => {
      return validateCreateOrderFunction(input);
    },
  };
