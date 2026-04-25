import {
  TagsStrongPassword,
  TagsUuid,
} from '@/utils/typia/validators/auth.validator';

export interface IDeleteSessionDto {
  sessionId: TagsUuid;
  password: TagsStrongPassword;
}
