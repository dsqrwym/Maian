import { Transform } from 'class-transformer';

interface TrimOptions {
  deep?: boolean;
}

/**
 * @Trim()
 * 去除字符串或字符串数组的首尾空格。
 * @param options.deep 递归处理对象/数组内部的字符串（默认 false）
 */
export function Trim(options: TrimOptions = {}): PropertyDecorator {
  return Transform(({ value }: { value: unknown }) => {
    if (options.deep) {
      return deepTrim(value);
    }
    return shallowTrim(value);
  });
}

function deepTrim(value: unknown): unknown {
  if (typeof value === 'string') {
    return value.trim();
  }

  if (Array.isArray(value)) {
    return value.map((v) => deepTrim(v));
  }

  if (value !== null && typeof value === 'object') {
    return Object.fromEntries(
      Object.entries(value).map(([k, v]) => [k, deepTrim(v)]),
    );
  }

  return value;
}

function shallowTrim(value: unknown): unknown {
  if (typeof value === 'string') {
    return value.trim();
  }

  if (Array.isArray(value)) {
    return value.map((v): unknown => (typeof v === 'string' ? v.trim() : v));
  }

  return value;
}
