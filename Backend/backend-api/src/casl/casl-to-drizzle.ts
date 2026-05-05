import type { AnyAbility } from '@casl/ability';
import type { SQL } from 'drizzle-orm';
import { and, eq, ne, notInArray, or } from 'drizzle-orm';

type DrizzleTable = Record<string, any>;
type Conditions = Record<string, unknown>;

/**
 * 将 CASL ability 中指定 action/subject 的条件规则转换为 Drizzle WHERE SQL 条件。
 *
 * 转换规则：
 * - can 规则：同一规则内多个字段 → AND，多个 can 规则之间 → OR
 * - cannot 规则：同字段聚合为 notInArray / ne
 * - can + cannot 组合 → and(canOrCond, ...cannotConds)
 * - 无条件规则（conditions 为空）→ 返回 undefined（表示不限制）
 *
 * 用法示例：
 * ```ts
 * const cond = caslToDrizzle(ability, Action.Read, 'products', products);
 * if (cond) whereConditions.push(cond);
 * ```
 */
export function caslToDrizzle(
  ability: AnyAbility,
  action: string,
  subject: string,
  table: DrizzleTable,
): SQL | undefined {
  /**
   * const rules = ability.rulesFor('read', 'products');
   * CASL 内部会合并所有匹配 read + products 的规则，包括 Manage（因为 Manage 包含 Read）。返回一个 Rule 数组，每条规则有：
   * 属性	含义	示例
   * inverted	false = can, true = cannot	—
   * conditions	MongoDB-style 条件对象	{ status: 'ACTIVE' } 或 { user_id: 'xxx' }
   * RETAILER 调用 rulesFor('read', 'products') 返回：
   *
   * [{ inverted: false, conditions: { status: 'ACTIVE' } }]
   */
  const rules = ability.rulesFor(action, subject);

  const canRules = rules.filter((rule) => !rule.inverted && rule.conditions);
  const cannotRules = rules.filter((rule) => rule.inverted && rule.conditions);

  // ── can 规则：每个 rule 内部 AND，多个 rule 之间 OR ──
  const canSqlParts: SQL[] = [];
  for (const rule of canRules) {
    const eqParts = buildEqConditions(table, rule.conditions as Conditions);
    if (eqParts.length === 1) canSqlParts.push(eqParts[0]);
    else if (eqParts.length > 1) canSqlParts.push(and(...eqParts)!);
  }

  // ── cannot 规则：同字段聚合为 notInArray / ne ──
  const cannotFieldMap = new Map<string, Set<unknown>>();
  for (const rule of cannotRules) {
    for (const [field, value] of Object.entries(
      rule.conditions as Conditions,
    )) {
      if (!table[field]) continue;
      if (!cannotFieldMap.has(field)) cannotFieldMap.set(field, new Set());
      cannotFieldMap.get(field)!.add(value);
    }
  }
  const cannotSqlParts: SQL[] = [];
  for (const [field, values] of cannotFieldMap) {
    const arr = [...values];
    cannotSqlParts.push(
      arr.length === 1
        ? ne(table[field], arr[0])
        : notInArray(table[field], arr),
    );
  }

  // ── 组合 can + cannot ──
  const parts: SQL[] = [];
  if (canSqlParts.length === 1) parts.push(canSqlParts[0]);
  else if (canSqlParts.length > 1) parts.push(or(...canSqlParts)!);

  if (cannotSqlParts.length > 0) parts.push(...cannotSqlParts);

  if (parts.length === 0) return undefined;
  if (parts.length === 1) return parts[0];
  return and(...parts);
}

/** 将 conditions 对象转换为 eq 条件数组，跳过表中不存在的字段 */
function buildEqConditions(table: DrizzleTable, conditions: Conditions): SQL[] {
  return Object.entries(conditions)
    .filter(([field]) => table[field])
    .map(([field, value]) => eq(table[field], value));
}

/**
 * 检查是否包含某些条件
 * @param ability
 * @param action
 * @param subject
 * @param field
 */
export function caslHasField(
  ability: AnyAbility,
  action: string,
  subject: string,
  field: string,
): boolean {
  return ability
    .rulesFor(action, subject)
    .some(
      (rule) => rule.conditions && field in (rule.conditions as Conditions),
    );
}
