import { TagsEmail } from '../../utils/typia/validators/auth.validator';
import typia, { tags } from 'typia';
import {
  TagsBCP47Language,
  TagsIANATimezone,
} from '../../utils/typia/validators/language.validator';
import { isObject } from '../../utils/is.utils';
import { cleanString } from '../../utils/string.util';
import { IRequestBodyValidator } from '@nestia/core/src/options/IRequestBodyValidator';

/**
 * DTO for sending normal registration email
 * 发送普通注册邮件的DTO
 */
export interface ISendNormalRegisterMailDto {
  /**
   * Email address for registration
   * 注册邮箱地址
   */
  email: string & TagsEmail;

  /**
   * Preferred language in BCP-47 format (e.g., en-US, zh-CN)
   * 首选语言（BCP-47格式，例如：en-US, zh-CN）
   */
  language: TagsBCP47Language;

  /**
   * Timezone in IANA format (e.g., America/New_York, Asia/Shanghai, Europe/Madrid)
   * 时区（IANA格式，例如：America/New_York, Asia/Shanghai, Europe/Madrid）
   */
  timezone: TagsIANATimezone;

  /**
   * Deep link for mobile app redirection (optional)
   * 移动应用深度链接（可选）
   */
  deepLink?: string & tags.MaxLength<500>;
}

export const validateSendNormalRegisterMail: IRequestBodyValidator.IAssert<ISendNormalRegisterMailDto> =
  {
    type: 'assert',
    assert: (input: unknown) => {
      if (isObject(input)) {
        const obj = input;
        if (typeof obj.email === 'string') {
          obj.email = cleanString(obj.email);
        }
      }

      return typia.assertEquals<ISendNormalRegisterMailDto>(input);
    },
  };
