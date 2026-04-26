import type {
  TagsEmail,
  TagsUsername,
  TagsWholesalerId,
} from '@/utils/typia/validators/auth.validator';
import { cleanString } from '@/utils/string.util';
import typia from 'typia';
import type { IRequestQueryValidator } from '@nestia/core/src/options/IRequestQueryValidator';

/**
 * DTO for checking email availability
 * 检查邮箱可用性的数据传输对象
 */
export interface ICheckUserEmailQueryDto {
  email: TagsEmail; // 邮箱地址
}
export const validateICheckUserEmailQueryDto: IRequestQueryValidator.IAssert<ICheckUserEmailQueryDto> =
  {
    type: 'assert',
    assert: (input: URLSearchParams): ICheckUserEmailQueryDto => {
      const email = input.get('email');
      if (email) {
        input.set('email', cleanString(email));
      }
      return typia.http.assertQuery<ICheckUserEmailQueryDto>(input);
    },
  };

/**
 * DTO for checking username availability
 * 检查用户名可用性的数据传输对象
 */
export interface ICheckUserUsernameQueryDto {
  username: TagsUsername;
  wholesalerId?: TagsWholesalerId;
  isAdmin?: boolean;
}
export const validateICheckUserUsernameQueryDto: IRequestQueryValidator.IAssert<ICheckUserUsernameQueryDto> =
  {
    type: 'assert',
    assert: (input: URLSearchParams): ICheckUserUsernameQueryDto => {
      const username = input.get('username');
      if (username) {
        input.set('name', cleanString(username));
      }
      return typia.http.assertQuery<ICheckUserUsernameQueryDto>(input);
    },
  };
