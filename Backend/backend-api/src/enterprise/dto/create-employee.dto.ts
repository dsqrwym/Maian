import {
  TagsEmail,
  TagsUsername,
} from '../../utils/typia/validators/auth.validator';
import typia, { tags } from 'typia';
import { TagsBasicTelephone } from '../../utils/typia/validators/telephone.validator';
import { isObject } from '../../utils/is.utils';
import { cleanString } from '../../utils/string.util';
import parsePhoneNumberFromString from 'libphonenumber-js';
import { BadRequestException } from '@nestjs/common';
import { IRequestBodyValidator } from '@nestia/core/src/options/IRequestBodyValidator';

/**
 * 批发商系统创建新员工的数据传输对象
 * 包含注册员工账户所需的所有必要信息
 *
 * @interface ICreateEmployeeDto
 */
export interface ICreateEmployeeDto {
  /**
   * 员工账户的唯一电子邮箱地址
   * 必须是有效格式且不在黑名单域名中
   *
   * @example 'employee.manager@company.com'
   */
  email: TagsEmail;

  /**
   * 员工的名字
   *
   * @example 'María'
   */
  first_name: string & tags.MaxLength<50>;

  /**
   * 员工的姓氏
   *
   * @example 'García López'
   */
  last_name?: string & tags.MaxLength<60>;

  /**
   * 联系电话
   *
   * @example '+34 600 123 456'
   */
  telephone?: TagsBasicTelephone;

  /**
   * 西班牙税务识别号 (CIF/NIF/NIE)
   *
   * @example 'B12345678'
   */
  cif?: string & tags.MaxLength<20> & tags.Pattern<'^[A-Z]?\\d{7,8}[A-Z]?$'>;

  /**
   * 系统登录用的唯一用户名
   * 长度必须为3-30个字符，且不能包含'@'符号
   *
   * @example 'mgarciam'
   */
  username?: TagsUsername;
}

export const validateICreateEmployee: IRequestBodyValidator.IAssert<ICreateEmployeeDto> =
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
        if (typeof obj.first_name === 'string') {
          obj.first_name = cleanString(obj.first_name);
        }
        if (typeof obj.last_name === 'string') {
          obj.last_name = cleanString(obj.last_name);
        }
        if (typeof obj.telephone === 'string') {
          const phone: string = cleanString(obj.telephone);
          const phoneNumber = parsePhoneNumberFromString(phone);
          if (!phoneNumber || !phoneNumber.isValid()) {
            throw new BadRequestException('Invalid phone number format');
          }
          obj.telephone = phone;
        }
        if (typeof obj.cif === 'string') {
          obj.cif = cleanString(obj.cif);
        }
      }
      return typia.assertEquals<ICreateEmployeeDto>(input);
    },
  };
