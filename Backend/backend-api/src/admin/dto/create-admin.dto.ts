import type {
  TagsEmail,
  TagsUsername,
} from '#/utils/typia/validators/auth.validator.js';
import { isObject } from '#/utils/is.utils.js';
import { cleanString } from '#/utils/string.util.js';
import typia from 'typia';
import type { IRequestBodyValidator } from '#/utils/typia/typia-type.js';

export interface ICreateAdminDto {
  /**
   * 员工账户的唯一电子邮箱地址
   * 必须是有效格式且不在黑名单域名中
   *
   * @example 'employee.manager@company.com'
   */
  email: TagsEmail;

  /**
   * 系统登录用的唯一用户名
   * 长度必须为3-30个字符，且不能包含'@'符号
   *
   * @example 'mgarciam'
   */
  username?: TagsUsername | null;
}
export const validateCreateAdminFunction =
  typia.createAssertEquals<ICreateAdminDto>();
export const validateCreateAdmin: IRequestBodyValidator.IAssert<ICreateAdminDto> =
  {
    type: 'assert',
    assert: (input: unknown) => {
      if (isObject(input)) {
        const obj = input;
        if (typeof obj.email === 'string') {
          obj.email = cleanString(obj.email);
        }
        if (typeof obj.username === 'string') {
          obj.username = cleanString(obj.username);
        }
      }

      return validateCreateAdminFunction(input);
    },
  };

export class VerifyEmployeeEmailJob {
  lang?: string;
  to: string;
  companyName: string;
  position: string;
  link: string;
}

export class ActiveEmployeeWithPasswordEmailJob {
  to: string;
  lang?: string;
  employeeName: string;
  companyName: string;
  temporaryPassword: string;
}
