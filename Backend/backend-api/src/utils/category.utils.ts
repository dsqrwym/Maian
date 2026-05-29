import type { SQL } from 'drizzle-orm';
import { sql } from 'drizzle-orm';

export function buildCategoryTranslationsJson(
  alias: string,
  langCode?: string,
): SQL {
  return sql`
    COALESCE((
      SELECT jsonb_agg(
        jsonb_build_object(
          'lang_code', ct.lang_code,
          'name', ct.name
        )
      )
      FROM category_translations ct
      WHERE ct.category_id = ${sql.raw(alias)}.id
      ${langCode ? sql`AND ct.lang_code = ${langCode}` : sql``}
    ), '[]'::jsonb)
  `;
}

export function buildCategoryJsonFields(
  alias: string,
  langCode?: string,
  iva?: boolean,
  level?: boolean,
  user_id?: boolean,
) {
  const parts = [
    sql`'id', ${sql.raw(alias)}.id`,
    sql`'name', ${sql.raw(alias)}.name`,
    sql`'category_translations', ${buildCategoryTranslationsJson(alias, langCode)}`,
  ];
  if (iva) parts.push(sql`'iva', ${sql.raw(alias)}.iva::text`);
  if (level) parts.push(sql`'level', ${sql.raw(alias)}.level`);
  if (user_id) parts.push(sql`'user_id', ${sql.raw(alias)}.user_id`);
  return sql.join(parts, sql`, `);
}
