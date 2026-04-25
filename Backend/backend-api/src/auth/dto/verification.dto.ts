import { ApiProperty } from '@nestjs/swagger';
import { ISendNormalRegisterMailDto } from './register.dto';
import typia, { tags } from 'typia';
import {
  TagsEmail,
  TagsUuid,
} from '@/utils/typia/validators/auth.validator';
import { TagsNotBlank } from '@/utils/typia/tags/string.tag';
import { TagsBCP47Language } from '@/utils/typia/validators/language.validator';
import { isObject } from '@/utils/is.utils';
import { cleanString } from '@/utils/string.util';
import { IRequestBodyValidator } from '@nestia/core/src/options/IRequestBodyValidator';

export type ISendVerificationCodeDto = Omit<
  ISendNormalRegisterMailDto,
  'language' | 'timezone'
>;
export const validateISendVerificationCode: IRequestBodyValidator.IAssert<ISendVerificationCodeDto> =
  {
    type: 'assert',
    assert: (input: unknown) => {
      if (input !== null && typeof input === 'object') {
        const obj = input as Record<string, unknown>;
        if (typeof obj.email === 'string') {
          obj.email = cleanString(obj.email);
        }
      }
      return typia.assertEquals<ISendVerificationCodeDto>(input);
    },
  };

export interface IVerifyCodeDto {
  code: string & tags.Pattern<'^\\d{6}$'> & tags.Example<'123456'>;
  email: string & TagsEmail; // 邮箱地址
}
export const validateVerifyCode: IRequestBodyValidator.IAssert<IVerifyCodeDto> =
  {
    type: 'assert',
    assert: (input: unknown) => {
      if (isObject(input)) {
        if (typeof input.email === 'string') {
          input.email = cleanString(input.email);
        }
        if (typeof input.code === 'string') {
          input.code = cleanString(input.code);
        }
      }

      return typia.assertEquals<IVerifyCodeDto>(input);
    },
  };

export interface IVerifyEmailQueryDto {
  userId: TagsUuid;

  token: TagsNotBlank;

  lang?: TagsBCP47Language;
}

export class VerifyCodeResponseDto {
  @ApiProperty({
    description:
      'Unique identifier for the password reset verification process',
    example: '123e4567-e89b-12d3-a456-426614174000',
  })
  verification_id: string;

  @ApiProperty({
    description: 'JWT token to be used for password reset',
    example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
  })
  token: string;

  @ApiProperty({
    description: 'Expiration timestamp of the verification',
    example: '2023-12-31T23:59:59.999Z',
  })
  expires_at: Date;
}
