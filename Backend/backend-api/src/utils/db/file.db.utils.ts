import type { DrizzleDb } from '#/drizzle/drizzle.service.js';
import { files } from '#/generated/drizzle/schema.js';
import { inArray } from 'drizzle-orm';

export async function restoreFilesFromCleanup(
  fileIds: bigint[],
  db: DrizzleDb,
): Promise<void> {
  const uniqueFileIds = [...new Set(fileIds)];
  if (uniqueFileIds.length === 0) return;

  await db
    .update(files)
    .set({ to_delete: false })
    .where(inArray(files.id, uniqueFileIds));
}
