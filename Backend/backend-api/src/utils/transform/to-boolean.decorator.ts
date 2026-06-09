/*
import { Transform } from 'class-transformer';

/!**
 * 可靠地把 Query / Body 里的字符串转为布尔值
 * 支持 "true" / "false" / 1 / 0 / true / false
 *!/
export const ToBoolean = () =>
  Transform(({ value }: { value: unknown }): boolean => {
    if (typeof value === 'boolean') return value;
    if (typeof value === 'string')
      return ['true', '1'].includes(value.toLowerCase());
    if (typeof value === 'number') return value === 1;
    return false;
  });
*/
