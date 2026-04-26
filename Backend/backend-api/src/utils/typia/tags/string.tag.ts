import type { tags } from 'typia';

/** 至少 N 个大写字母 */
export type TagsMinUppercase<N extends number> = string &
  tags.TagBase<{
    kind: 'string.minUppercase';
    target: 'string';
    value: N;
    validate: `
      ($input.match(/\\p{Lu}/gu)?.length ?? 0) >= ${N}
    `;
  }>;

/** 至少 N 个小写字母 */
export type TagsMinLowercase<N extends number> = string &
  tags.TagBase<{
    kind: 'string.minLowercase';
    target: 'string';
    value: N;
    validate: `
      ($input.match(/\\p{Ll}/gu)?.length ?? 0) >= ${N}
    `;
  }>;

/** 至少 N 个数字 */
export type TagsMinDigits<N extends number> = string &
  tags.TagBase<{
    kind: 'string.minDigits';
    target: 'string';
    value: N;
    validate: `
      ($input.match(/\\d/g)?.length ?? 0) >= ${N}
    `;
  }>;

/** 不可以以指定字符串结尾（忽略大小写）, 要求输入为小写 */
export type TagsNotEndWithIgnoreCase<Value extends string> = string &
  tags.TagBase<{
    kind: 'string.notEndWithIgnoreCase';
    target: 'string';
    value: Value;
    validate: `!$input.toLowerCase().endsWith("${Value}")`;
  }>;

/** 不可以包含指定字符串（区分大小写） */
export type TagsNotInclude<Value extends string> = string &
  tags.TagBase<{
    kind: 'string.notInclude';
    target: 'string';
    value: Value;
    validate: `!$input.includes("${Value}")`;
  }>;

/** 不为空或者只用空格 */
export type TagsNotBlank = string &
  tags.TagBase<{
    kind: 'string.notBlank';
    target: 'string';
    value: undefined;
    validate: `$input.trim().length > 0`;
  }>;

/** BCP-47 语言代码同步校验（不缓存） */
export type TagsBCP47 = string &
  tags.TagBase<{
    kind: 'string.bcp47';
    target: 'string';
    value: undefined;
    validate: `
    (() => {
      try { new Intl.DateTimeFormat($input); return true; }
      catch { return false; }
    })()
  `;
  }>;
// (函数) -》 作用为将函数转为表达式，这个表达式放回的值为函数本身
// （）（）变为立刻执行,将表达式的果改为函数结果

/**
 * IANA格式 时区校验（不缓存）
 */
export type TagsIANA = string &
  tags.TagBase<{
    kind: 'string.IANA';
    target: 'string';
    value: undefined;
    validate: `
    (() => {
      try { new Intl.DateTimeFormat(undefined, { timeZone: $input }); return true; }
      catch { return false; }
    })()
  `;
  }>;

/**
 * string 必须是合法整数（用于 bigint 传输）
 */
export type TagsIntegerString = string &
  tags.TagBase<{
    kind: 'string.numeric';
    target: 'string';
    value: undefined;
    validate: `/^-?\\d+$/.test($input)` & tags.Example<'123'>;
  }>;
