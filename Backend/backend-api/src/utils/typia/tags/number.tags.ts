import type { tags } from 'typia';

/**
 * 字符串表示的数字必须大于等于 N
 */
export type TagsMinNumberString<N extends number> = string &
  tags.TagBase<{
    kind: 'string.minNumber';
    target: 'string';
    value: N;
    validate: `
      (() => {
        const num = Number($input);
        return !isNaN(num) && num >= ${N};
      })()
    `;
  }>;

/**
 * 字符串表示的数字必须小于等于 N
 */
export type TagsMaxNumberString<N extends number> = string &
  tags.TagBase<{
    kind: 'string.maxNumber';
    target: 'string';
    value: N;
    validate: `
      (() => {
        const num = Number($input);
        return !isNaN(num) && num <= ${N};
      })()
    `;
  }>;

/**
 * PostgreSQL numeric (numeric(10,2))
 * 范围: 0 ~ 99999999.99
 */
export type TagsUNumeric10_2_String = string &
  tags.TagBase<{
    kind: 'string.numeric10_2';
    target: 'string';
    value: undefined;
    validate: `
      /^(?:0|[1-9]\\d{0,7})(\\.\\d{1,2})?$/.test($input)
    `;
  }>;

/**
 * PostgreSQL smallint (int2)
 * 范围: -32768 ~ 32767
 */
export type TagsInt2 = number &
  tags.Type<'int32'> & // 保证是整数语义
  tags.Minimum<-32768> &
  tags.Maximum<32767>;

/**
 * PostgreSQL smallint (int2)
 * 无符号 smallint (业务约束)
 * 范围: 0 ~ 32767
 */
export type TagsUInt2 = number &
  tags.Type<'int32'> &
  tags.Minimum<0> &
  tags.Maximum<32767>;

/**
 * PostgreSQL integer (int4)
 * 无符号 integer (业务约束)
 * 范围: 0 ~ 2147483647
 */
export type TagsUInt4 = number &
  tags.Type<'int32'> &
  tags.Minimum<0> &
  tags.Maximum<2147483647>;

/**
 * PostgreSQL bigint
 * 但是 number 精度丢失 不过对于 version 足够了
 */
export type TagsVersion = number &
  tags.Type<'int64'> &
  tags.Minimum<0> &
  tags.Maximum<9007199254740991>; // Number.MAX_SAFE_INTEGER
