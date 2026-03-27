/**
 * 配置项接口
 */
interface TrimOptions<T> {
  fields?: (keyof T & string)[];
  deep?: boolean;
}

function isString(value: unknown): value is string {
  return typeof value === 'string';
}

function isObject(value: unknown): value is Record<string, unknown> {
  return value !== null && typeof value === 'object' && !Array.isArray(value);
}

/**
 * 对对象/数组进行首尾空格清理
 */
export function trimObject<T>(input: T, options: TrimOptions<T> = {}): T {
  const { fields, deep = false } = options;

  // 处理基本类型字符串
  if (isString(input)) {
    return input.trim() as unknown as T;
  }

  // 处理数组
  if (Array.isArray(input)) {
    const trimmedArray: unknown = input.map((v: unknown) => {
      if (deep) return trimObject(v, { deep: true });
      return isString(v) ? v.trim() : v;
    });
    return trimmedArray as T;
  }

  // 处理对象
  if (isObject(input)) {
    // 关键修复：将副本断言为可写的 Record
    const result = { ...input } as Record<string, unknown>;
    const keysToProcess: string[] = fields ?? Object.keys(result);

    for (const key of keysToProcess) {
      const value = result[key];

      if (isString(value)) {
        result[key] = value.trim();
      } else if (deep && isObject(value)) {
        // 递归处理子对象
        result[key] = trimObject(value, { deep: true });
      }
    }
    return result as unknown as T;
  }

  // 其他类型直接返回
  return input;
}
