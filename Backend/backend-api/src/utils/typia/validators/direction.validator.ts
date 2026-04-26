import type { tags } from 'typia';

/**
 * 纬度
 */
export type TagsLatitude = number &
  tags.Minimum<-90> &
  tags.Maximum<90> &
  tags.Example<'39.7128'>;

/**
 * 经度
 */
export type TagsLongitude = number &
  tags.Minimum<-180> &
  tags.Maximum<180> &
  tags.Example<'116.4074'>;
