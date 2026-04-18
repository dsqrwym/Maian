import { IDirectionDto, validateDirection } from './register.direction.dto';
import {
  TagsEmail,
  TagsStrongPassword,
  TagsUsername,
  TagsUuid,
} from '../../utils/typia/validators/auth.validator';
import { TagsNotBlank } from '../../utils/typia/tags/string.tag';
import typia, { tags } from 'typia';
import parsePhoneNumberFromString from 'libphonenumber-js';
import { BadRequestException } from '@nestjs/common';
import { isObject } from '../../utils/is.utils';
import { cleanString } from '../../utils/string.util';
import { TagsBasicTelephone } from '../../utils/typia/validators/telephone.validator';
import { IRequestBodyValidator } from '@nestia/core/src/options/IRequestBodyValidator';

/**
 * Enum for Spanish company types
 * 西班牙公司类型枚举
 */
export enum SpanishCompanyType {
  SL = 'S.L.', // Sociedad Limitada（有限责任公司）
  SA = 'S.A.', // Sociedad Anónima（股份有限公司）
  AUTONOMO = 'Autónomo', // 个体户
  COOPERATIVA = 'Cooperativa', // 合作社
  SOCIEDAD_CIVIL = 'Sociedad Civil', // 民事公司
  OTROS = 'Otros',
}

/**
 * DTO for wholesaler registration
 * 批发商注册数据传输对象
 */
export interface IRegisterWholesalerDto {
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
   * Username (optional)
   * 用户名（可选）
   */
  username?: TagsUsername;

  /**
   * Company's legal name
   * 公司名称
   */
  company_name: TagsNotBlank & tags.MaxLength<100>;

  /**
   * Company type (Spain)
   * 公司类型（西班牙）
   */
  company_type: SpanishCompanyType;

  /**
   * Telephone number
   * 联系电话
   */
  telephone: TagsBasicTelephone;

  /**
   * Business address
   * 经营地址
   */
  address: IDirectionDto;

  /**
   * Verification ID
   * 验证ID
   */
  verification_id: TagsUuid;

  /**
   * JWT token used for verification
   * 用于验证的JWT令牌
   */
  token: TagsNotBlank;
}

export const validateRegisterWholesaler: IRequestBodyValidator.IAssert<IRegisterWholesalerDto> =
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
        if (typeof obj.company_name === 'string') {
          obj.company_name = cleanString(obj.company_name);
        }
        if (typeof obj.telephone === 'string') {
          obj.telephone = cleanString(obj.telephone);
        }
        obj.address = validateDirection(obj.address);
      }
      const typedBody = typia.assertEquals<IRegisterWholesalerDto>(input);

      const phoneNumber = parsePhoneNumberFromString(typedBody.telephone);
      if (!phoneNumber || !phoneNumber.isValid()) {
        throw new BadRequestException('Invalid phone number format');
      }

      return typedBody;
    },
  };
