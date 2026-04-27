import type { tags } from 'typia';
import type {
  TagsMinDigits,
  TagsMinLowercase,
  TagsMinUppercase,
  TagsNotBlank,
  TagsNotEndWithIgnoreCase,
  TagsNotInclude,
} from '../tags/string.tag.js';

/**
 * 强密码验证
 * 最小长度为6 tags.MinLength<6>
 * 要求至少包含一个数字、 MinDigits<1>
 * 一个大写字母、MinUppercase<1>
 * 一个小写字母 MinLowercase<1>
 */
export type TagsStrongPassword = tags.MinLength<6> &
  TagsMinUppercase<1> &
  TagsMinLowercase<1> &
  TagsMinDigits<1> &
  tags.Example<'StrongPassword123!'>;

/**
 * 邮箱验证
 * 最大长度为100 MaxLength<100>
 * 格式为邮箱 Format<'email'>
 * 不能以 @example.com 结尾 NotEndWithIgnoreCase<'@example.com'>
 */
export type TagsEmail = TagsNotBlank &
  tags.MaxLength<100> &
  tags.Format<'email'> &
  TagsNotEndWithIgnoreCase<'@example.com'> &
  tags.Example<'user@example.com'>;

/**
 * 用户名验证
 * 最小长度为3 MinLength<3>
 * 最大长度为30 MaxLength<30>
 * 不能包含 @NotInclude<'@'>
 */
export type TagsUsername = string &
  tags.MinLength<3> &
  tags.MaxLength<30> &
  TagsNotInclude<'@'> &
  tags.Example<'username'>;

/**
 * 批发商ID验证
 * 最小长度为3 MinLength<3>
 * 最大长度为20 MaxLength<20>
 */
export type TagsWholesalerId = string & tags.MinLength<3> & tags.MaxLength<20>;

/**
 * 登陆设备名称
 * 最长150 tags.MaxLength<150>
 */
export type TagsDeviceName = TagsNotBlank &
  tags.MaxLength<150> &
  tags.Example<'CHROME_BROWSER'>; // 登录设备名称

/**
 * 登陆设备
 * 最长255  tags.MaxLength<255>
 */
export type TagsUserAgent = TagsNotBlank &
  tags.MaxLength<255> &
  tags.Example<'MOZILLA/5.0 (WINDOWS NT 10.0; WIN64; X64)'>; // 登录设备

/**
 * 检验UUID，带例子
 */
export type TagsUuid = TagsNotBlank &
  tags.Format<'uuid'> &
  tags.Example<'1e2d3c4b-5a6f-7890-abcd-ef1234567890'>;
