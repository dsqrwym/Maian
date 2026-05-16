import type { tags } from 'typia';
import typia from 'typia';
import type { IRequestBodyValidator } from '#/utils/typia/typia-type.js';
import { isObject } from '#/utils/is.utils.js';

export interface IRejectOrderDto {
  actionReason: string & tags.MaxLength<500>;
}
export const validateIRejectOrderDtoFunction =
  typia.createAssertEquals<IRejectOrderDto>();
export const validateIRejectOrderDto: IRequestBodyValidator.IAssert<IRejectOrderDto> =
  {
    type: 'assert',
    assert: (input: unknown) => {
      if (isObject(input)) {
        if (typeof input.actionReason === 'string') {
          input.actionReason = input.actionReason.trim();
        }
      }
      return validateIRejectOrderDtoFunction(input);
    },
  };

export interface ICancelOrderDto {
  actionReason: (string & tags.MaxLength<500>) | null;
}
export const validateICancelOrderDtoFunction =
  typia.createAssertEquals<ICancelOrderDto>();
export const validateICancelOrderDto: IRequestBodyValidator.IAssert<ICancelOrderDto> =
  {
    type: 'assert',
    assert: (input: unknown) => {
      if (isObject(input)) {
        if (typeof input.actionReason === 'string') {
          input.actionReason = input.actionReason.trim();
        }
      }
      return validateICancelOrderDtoFunction(input);
    },
  };
