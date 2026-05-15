import { and, asc, eq, like, sql } from 'drizzle-orm';
import { files, products, products_files } from '#/generated/drizzle/schema.js';
import type { DrizzleDb } from '#/drizzle/drizzle.service.js';

export function buildMainImgLateral(db: DrizzleDb) {
  return db
    .select({
      main_image: sql<{ id: string; mime_type: string } | null>`
      jsonb_build_object(
        'id', ${files.id},
        'mime_type', ${files.mime_type}
      )
    `.as('main_image'),
    })
    .from(products_files)
    .innerJoin(files, eq(files.id, products_files.file_id))
    .where(
      and(
        eq(products_files.product_id, products.id),
        like(files.mime_type, 'image/%'),
      ),
    )
    .orderBy(asc(products_files.sort))
    .limit(1)
    .as('mainImgLateral');
}
