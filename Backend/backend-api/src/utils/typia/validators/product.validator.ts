import type { tags } from 'typia';
import type {
  TagsMaxNumberString,
  TagsUNumeric10_2_String,
} from '../tags/number.tags.js';

/**
 *  税率百分比验证，最大100，最少0，最多两位小数
 */
export type TagsIvaString = string &
  tags.TagBase<{
    kind: 'string.iva';
    target: 'string';
    value: undefined;
    validate: `
      (() => {
        if (!/^\\d+(\\.\\d{1,2})?$/.test($input)) return false;

        const n = Number($input);
        return n >= 0 && n <= 100;
      })()
    `;
  }> &
  tags.Example<'21.00'>;

/**
 * 价格验证，最大一千万，最少0，最多两位小数
 */
export type TagsPrice = TagsUNumeric10_2_String &
  TagsMaxNumberString<10000000.0> &
  tags.Example<'10.0'>;

/**
 * 含税价格验证，最大两千万，最少0，最多两位小数
 */
export type TagsPriceIva = TagsUNumeric10_2_String &
  TagsMaxNumberString<20000000.0> &
  tags.Example<'10.21'>;

/**
 * ProductCode 类型：
 * - 大小写字母 A-Z a-z
 * - 数字 0-9
 * - 符号 _ - / .
 * - 至少 1 个字符，最多 50 个字符
 * - 禁止空格和特殊字符，防止 URL/HTML 注入
 * | 条码类型      | 示例          | 兼容性
 * | ----------- | -------------- | ------------------------------------------
 * | EAN-13      | 1234567890123  | 纯数字，长度 ≤50
 * | UPC-A       | 123456789012   | 纯数字，长度 ≤50
 * | ITF-14      | 00614141999996 | 纯数字，长度 ≤50
 * | Code39      | ABC-123*%      | 字母、数字、部分符号
 * | Code128     | Ab123          | 大小写字母、数字
 * | EAN/UCC-128 | (01)2534668941 | 数字 + 括号/ASCII 可以用 `_` `/` `.` 替代括号符号安全映射
 */
export type TagsProductCode = string &
  tags.Pattern<'^[A-Za-z0-9/_\\.-]{1,50}$'> &
  tags.Example<'EAN-123, UPC-A, ITF-14, Code39, Code128, EAN/UCC-128'>;
