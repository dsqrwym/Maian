import type { TagsUuid } from '@/utils/typia/validators/auth.validator';

export interface IUploadFileForWholesalerDto {
  wholesalerId?: string & TagsUuid;
}
