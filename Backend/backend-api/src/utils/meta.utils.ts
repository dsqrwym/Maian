export function safeMeta(metaField: unknown): string {
  if (typeof metaField === 'string' || typeof metaField === 'number') {
    return String(metaField);
  }
  if (metaField == null) {
    return '';
  }
  return JSON.stringify(metaField);
}

export function extractPrismaMeta(
  meta: Record<string, unknown> | undefined,
): string {
  return (
    safeMeta(meta?.field_name) ||
    safeMeta(meta?.constraint) ||
    safeMeta(meta?.target) ||
    safeMeta(meta?.relation_name) ||
    safeMeta(meta?.column_name) ||
    safeMeta(meta?.table) ||
    safeMeta(meta?.cause) ||
    ''
  );
}
