/**
 * 放回合并后的更新即只更新于新值不一样的
 * @param current
 * @param dto
 */
export function buildMergedUpdate<T extends object>(
  current: T,
  dto: Partial<T>,
): Partial<T> | undefined {
  const merged: Partial<T> = {};
  let hasChange = false;
  for (const key of Object.keys(dto) as Array<keyof T>) {
    const newValue = dto[key];
    // 没有变化放回原值
    if (newValue === undefined) {
      merged[key] = current[key];
      continue;
    }
    // 不一样执行更新
    if (current[key] !== newValue) {
      merged[key] = newValue;
      hasChange = true;
    }
  }

  return hasChange ? merged : undefined;
}
