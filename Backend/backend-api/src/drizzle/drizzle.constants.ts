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
export const SQL_IMMUTABLE_UNACCENT = (v) => sql`immutable_unaccent(${v})`;
