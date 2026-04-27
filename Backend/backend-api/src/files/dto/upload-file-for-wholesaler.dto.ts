import type { TagsUuid } from '#/utils/typia/validators/auth.validator.js';

export interface IUploadFileForWholesalerDto {
  wholesalerId?: string & TagsUuid;
}
