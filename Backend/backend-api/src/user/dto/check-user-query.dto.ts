import type {
  TagsEmail,
  TagsUsername,
  TagsWholesalerId,
} from '#/utils/typia/validators/auth.validator.js';
import { cleanString } from '#/utils/string.util.js';
import typia from 'typia';
import type { IRequestQueryValidator } from '#/utils/typia/typia-type.js';
import type { TagsIntegerString } from '#/utils/typia/tags/string.tag.js';

/**
 * DTO for checking email availability
 * 检查邮箱可用性的数据传输对象
 */
export interface ICheckUserEmailQueryDto {
  email: TagsEmail; // 邮箱地址
}
export const validateICheckUserEmailQueryDtoFunction =
  typia.http.createAssertQuery<ICheckUserEmailQueryDto>();
export const validateICheckUserEmailQueryDto: IRequestQueryValidator.IAssert<ICheckUserEmailQueryDto> =
  {
    type: 'assert',
    assert: (input: URLSearchParams): ICheckUserEmailQueryDto => {
      const email = input.get('email');
      if (email) {
        input.set('email', cleanString(email));
      }
      return validateICheckUserEmailQueryDtoFunction(input);
    },
  };

/**
 * DTO for checking username availability
 * 检查用户名可用性的数据传输对象
 */
export interface ICheckUserUsernameQueryDto {
  userId?: TagsIntegerString;
  username: TagsUsername;
  wholesalerId?: TagsWholesalerId;
  isAdmin?: boolean;
}
export const validateICheckUserUsernameQueryDtoFunction =
  typia.http.createAssertQuery<ICheckUserUsernameQueryDto>();
export const validateICheckUserUsernameQueryDto: IRequestQueryValidator.IAssert<ICheckUserUsernameQueryDto> =
  {
    type: 'assert',
    assert: (input: URLSearchParams): ICheckUserUsernameQueryDto => {
      const username = input.get('username');
      if (username) {
        input.set('name', cleanString(username));
      }
      return validateICheckUserUsernameQueryDtoFunction(input);
    },
  };

/**
 * 检查当前用户的身份范围是否已经使用了
 */
export interface ICheckUserTaxIdQueryDto {
  id?: TagsIntegerString;
  taxId: string;
}
export const validateICheckUserTaxIdQueryDtoFunction =
  typia.http.createAssertQuery<ICheckUserTaxIdQueryDto>();
export const validateICheckUserTaxIdQueryDto: IRequestQueryValidator.IAssert<ICheckUserTaxIdQueryDto> =
  {
    type: 'assert',
    assert: (input: URLSearchParams): ICheckUserTaxIdQueryDto => {
      const taxId = input.get('taxId');
      if (taxId) {
        input.set('taxId', cleanString(taxId));
      }
      return validateICheckUserTaxIdQueryDtoFunction(input);
    },
  };
