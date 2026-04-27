import type {
  TagsStrongPassword,
  TagsUuid,
} from '#/utils/typia/validators/auth.validator.js';

export interface IResetPasswordDto {
  verification_id: string & TagsUuid;

  token: string & TagsUuid;

  newPassword: string & TagsStrongPassword; // 密码
}
