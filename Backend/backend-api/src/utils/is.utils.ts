/**
 * 判断是否为对象。
 * JavaScript的 Bug typeof null 的结果是 "object"
 * 所以需要额外判断
 * @param val
 */
export const isObject = (val: unknown): val is Record<string, unknown> => {
  return val !== null && typeof val === 'object';
};
/**
 * 判断是否为json字符串
 * 主要用于判断是否来自 typia stringify 过的
 * @param data
 */
export const isJson = (data: unknown): data is string => {
  if (typeof data !== 'string') return false;
  if (data.length < 2) return false;
  const first = data[0];
  const last = data[data.length - 1];

  return (
    (first === '{' && last === '}') ||
    (first === '[' && last === ']') ||
    (first === '"' && last === '"')
  );

  // try {
  //   JSON.parse(data);
  //   return true;
  // } catch {
  //   return false;
  // }
};
