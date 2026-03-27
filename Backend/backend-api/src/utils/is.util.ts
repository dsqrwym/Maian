/**
 * 判断是否为对象。
 * JavaScript的 Bug typeof null 的结果是 "object"
 * 所以需要额外判断
 * @param val
 */
export const isObject = (val: unknown): val is Record<string, unknown> => {
  return val !== null && typeof val === 'object';
};
