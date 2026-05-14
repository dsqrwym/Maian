import type { DrizzleDb } from '#/drizzle/drizzle.service.js';
import { files, user_uploads, users } from '#/generated/drizzle/schema.js';
import { and, eq, exists, ne, sql } from 'drizzle-orm';
import { BadRequestException } from '@nestjs/common';
import { IMAGE_MIME_TYPES } from '#/config/fastify-multipart.config.js';
import type { UserRole } from '#/generated/drizzle/enums.js';
import { SQL_TEMP_TABLE } from '#/drizzle/drizzle.constants.js';

/**
 * 验证并检查文件是否为正确类型以及属于当前用户
 * @param fileId
 * @param ownerId
 * @param db
 * @private
 */
export async function validateAndCheckUserFiles(
  fileId: string,
  ownerId: string,
  db: DrizzleDb,
): Promise<void> {
  const [validateFiles] = await db
    .select({ mime_type: files.mime_type })
    .from(files)
    .innerJoin(user_uploads, eq(user_uploads.file_id, files.id))
    .where(and(eq(files.id, BigInt(fileId)), eq(user_uploads.user_id, ownerId)))
    .limit(1);

  if (!validateFiles) {
    throw new BadRequestException('File not found');
  }

  const mime_type = validateFiles.mime_type;

  if (!IMAGE_MIME_TYPES.has(mime_type)) {
    throw new BadRequestException(`Invalid file mime type: ${mime_type}`);
  }
}

/**
 * 验证 对应用户身份的税号是否唯一
 * @param ownerId
 * @param taxId
 * @param role
 * @param db
 */
export async function checkUserTaxId(
  ownerId: string,
  taxId: string,
  role: UserRole,
  db: DrizzleDb,
) {
  const alreadyExistTaxId = db
    .select({ tax_id: users.tax_id })
    .from(users)
    .where(
      and(eq(users.role, role), eq(users.tax_id, taxId), ne(users.id, ownerId)),
    );

  const existing = (await db
    .select({ exists: exists(alreadyExistTaxId) })
    .from(SQL_TEMP_TABLE)
    .execute()) as { exists: boolean }[];

  if (existing[0]?.exists) {
    throw new BadRequestException('Tax ID already exists');
  }
}

/**
 * 构建 wholesaler profile 的字段获取
 * @param profile
 */
export function buildWholesalerProfileExpr(profile: typeof users.profile) {
  return {
    companyTypeExpr: sql<string>`${profile}->>'company_type'`,
    companyNameExpr: sql<string>`${profile}->>'company_name'`,
    displayNameExpr: sql<
      string | null | undefined
    >`${profile}->>'display_name'`,
    descriptionExpr: sql<string | null | undefined>`${profile}->>'description'`,
    deliveryAreaDescriptionExpr: sql<
      string | null | undefined
    >`${profile}->>'delivery_area_description'`,
    minimumOrderAmountExpr: sql<
      string | null | undefined
    >`${profile}->>'minimum_order_amount'`,
    deliveryAvailableExpr: sql<
      boolean | null | undefined
    >`(${profile}->>'delivery_available')::boolean`,
    pickupAvailableExpr: sql<
      boolean | null | undefined
    >`(${profile}->>'pickup_available')::boolean`,
  } as const;
}

export function buildRetailerProfileExpr(profile: typeof users.profile) {}
