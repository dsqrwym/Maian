import type { AnyColumn, SQL } from 'drizzle-orm';
import { sql } from 'drizzle-orm';

/**
 * 用于 设置数据库当前 UTC 时间
 */
export const SQL_NOW = sql`(NOW() AT TIME ZONE 'UTC')`;
/**
 * 用于 drizzle select exit 的 from 临时表的占位符
 */
export const SQL_TEMP_TABLE = sql`(VALUES (1)) AS tmp`;
/**
 * 用于 drizzle left join lateral 的 on 占位符
 */
export const SQL_TRUE = sql`TRUE`;
/**
 * 用于 drizzle 查询进行去重音
 */
export const SQL_IMMUTABLE_UNACCENT = (v: any) => sql`immutable_unaccent(${v})`;

type JsonObjectShape = Record<string, AnyColumn | SQL | SQL.Aliased>;

export const jsonbBuildObject = <T>(shape: JsonObjectShape): SQL<T> => {
  const chunks: SQL[] = [];

  for (const [key, value] of Object.entries(shape)) {
    chunks.push(sql`${sql.raw(`'${key}'`)}, ${value}`);
  }

  return sql<T>`jsonb_build_object(${sql.join(chunks, sql.raw(', '))})`;
};

export const jsonbAgg = <T>(
  expression: SQL<T>,
  options?: {
    orderBy?: SQL;
    filter?: SQL;
  },
): SQL<T[]> => {
  return sql<T[]>`
    coalesce(
      jsonb_agg(
        ${expression}
        ${options?.orderBy ? sql`order by ${options.orderBy}` : sql``}
      )
      ${options?.filter ? sql`filter (where ${options.filter})` : sql``},
      '[]'::jsonb
    )
  `;
};

export const jsonbAggBuildObject = <T>(
  shape: JsonObjectShape,
  options?: {
    orderBy?: SQL;
    filter?: SQL;
  },
): SQL<T[]> => {
  return jsonbAgg<T>(jsonbBuildObject<T>(shape), options);
};
