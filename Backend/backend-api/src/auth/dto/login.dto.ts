import type {
  TagsDeviceName,
  TagsEmail,
  TagsStrongPassword,
  TagsUserAgent,
  TagsUsername,
  TagsWholesalerId,
} from '#/utils/typia/validators/auth.validator.js';
import { BadRequestException } from '@nestjs/common';
import typia from 'typia';
import { isObject } from '#/utils/is.utils.js';
import { cleanString } from '#/utils/string.util.js';
import type { IRequestBodyValidator } from '#/utils/typia/typia-type.js';

export interface ILoginDto {
  password: string & TagsStrongPassword; // 密码

  email?: string & TagsEmail; // 邮箱地址

  username?: string & TagsUsername; // 用户名

  wholesalerId?: string & TagsWholesalerId;

  deviceName: string & TagsDeviceName; // 登录设备名称

  userAgent: string & TagsUserAgent; // 登录设备
}
export const validateLoginFunction = typia.createAssertEquals<ILoginDto>();

export const validateLogin: IRequestBodyValidator.IAssert<ILoginDto> = {
  type: 'assert',
  assert: (input: unknown) => {
    if (isObject(input)) {
      const obj = input;
      // 自分配（避免额外对象分配）, javascript对象赋值给另一个变量时，只是复制了指针
      if (typeof obj.email === 'string') {
        obj.email = cleanString(obj.email);
      }
      if (typeof obj.username === 'string') {
        obj.username = cleanString(obj.username);
      }
      if (typeof obj.deviceName === 'string') {
        obj.deviceName = obj.deviceName.toUpperCase();
      }
      if (typeof obj.userAgent === 'string') {
        obj.userAgent = obj.userAgent.toUpperCase();
      }
    }
    const typedInput = validateLoginFunction(input);

    if (!typedInput.email && !typedInput.username) {
      throw new BadRequestException('Either email or username is required');
    }

    return typedInput;
  },
};
