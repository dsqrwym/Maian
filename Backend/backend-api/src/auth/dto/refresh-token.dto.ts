import { TagsNotBlank } from '@/utils/typia/tags/string.tag';
import { tags } from 'typia';

export interface IRefreshTokenDto {
  refreshToken: string &
    TagsNotBlank &
    tags.Example<'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'>;
}
