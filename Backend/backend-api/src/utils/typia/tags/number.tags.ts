import { tags } from 'typia';

/**
 * 验证number是否最多两个小数。
 * tags.MultipleOf<0.01>由于JS number 精度的问题不起效果：
 * 比如21.90不是MultipleOf<0.01>的，会被当成21.8999999999。
 * 实现逻辑：将数放大 100 倍，四舍五入后与原数比较，
 * 如果差值 < 1e-8(0.00000001) 则合法，避免浮点误差，高性能内联校验。
 * 21.9 * 100 = 2189.9999999999995
 * Math.round(2189.9999999999995) = 2190
 * 2189.9999999999995 - 2190 = -0.0000000000005
 */
export type TagsTowDecimal = number &
  tags.TagBase<{
    kind: 'number.tow-decimal';
    target: 'number';
    value: undefined;
    validate: `Math.abs($input * 100 - Math.round($input * 100)) < 1e-8`;
  }>;

/**
 * PostgreSQL numeric (numeric(10,2))
 * 范围: 0 ~ 9999999999.99
 */
export type TagsUNumeric10_2 = number & tags.Minimum<0> & TagsTowDecimal;

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
