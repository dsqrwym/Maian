import type {
  TagsStrongPassword,
  TagsUuid,
} from '#/utils/typia/validators/auth.validator.js';

export interface IDeleteSessionDto {
  sessionId: TagsUuid;
  password: TagsStrongPassword;
}
