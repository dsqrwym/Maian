import { IDirectionDto, validateDirection } from './register.direction.dto';
import {
  TagsEmail,
  TagsStrongPassword,
  TagsUsername,
  TagsUuid,
} from '../../utils/typia/validators/auth.validator';
import { TagsNotBlank } from '../../utils/typia/tags/string.tag';
import typia from 'typia';
import { isObject } from '../../utils/is.utils';
import { cleanString } from '../../utils/string.util';
import { IRequestBodyValidator } from '@nestia/core/src/options/IRequestBodyValidator';

/**
 * DTO for retailer registration
 * 零售商注册数据传输对象
 */
export interface IRegisterRetailerDto {
  /**
   * User's email address
   * 用户邮箱地址
   */
  email: string & TagsEmail;

  /**
   * User's password
   * 用户密码
   */
  password: string & TagsStrongPassword;

  /**
   * Username (optional, must be unique)
   * 用户名（可选，必须唯一）
   */
  username?: string & TagsUsername;

  /**
   * Store's business address
   * 商店经营地址
   */
  address: IDirectionDto;

  /**
   * Unique identifier for the password reset verification process
   */
  verification_id: TagsUuid;

  /**
   * JWT token to be used for password reset
   */
  token: TagsNotBlank;
}

export const validateRegisterRetailer: IRequestBodyValidator.IAssert<IRegisterRetailerDto> =
  {
    type: 'assert',
    assert: (input: unknown): IRegisterRetailerDto => {
      if (isObject(input)) {
        const obj = input;
        if (typeof obj.email === 'string') {
          obj.email = cleanString(obj.email);
        }
        if (typeof obj.username === 'string') {
          obj.username = cleanString(obj.username);
        }
        obj.address = validateDirection(obj.address);
      }

      return typia.assertEquals<IRegisterRetailerDto>(input);
    },
  };
