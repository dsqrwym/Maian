import { DrizzleDb, DrizzleService } from '#/drizzle/drizzle.service.js';
import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { IUpdateWholesalerProfileDto } from '#/user/dto/update-wholesaler-profile.dto.js';
import { AppAbility } from '#/casl/casl-types.js';
import { Action } from '#/casl/actions.js';
import { files, user_uploads, users } from '#/generated/drizzle/schema.js';
import { and, eq, exists, ne, sql } from 'drizzle-orm';
import { IWholesalerProfile } from '#/enterprise/types/IWholesalerProfile.js';
import { buildMergedUpdate } from '#/utils/patch.utils.js';
import { IMAGE_MIME_TYPES } from '#/config/fastify-multipart.config.js';
import { UserRole } from '#/generated/drizzle/enums.js';

@Injectable()
export class WholesalerProfileService {
  constructor(private readonly drizzle: DrizzleService) {}

  private async checkWholesalerTaxId(
    ownerId: string,
    taxId: string,
    db: DrizzleDb,
  ) {
    const alreadyExistWholesalerTaxId = db
      .select({ tax_id: users.tax_id })
      .from(users)
      .where(
        and(
          eq(users.role, UserRole.WHOLESALER),
          eq(users.tax_id, taxId),
          ne(users.id, ownerId),
        ),
      );

    const existing = (await db
      .select({ exists: exists(alreadyExistWholesalerTaxId) })
      .from(sql`(VALUES (1)) AS tmp`)
      .execute()) as { exists: boolean }[];

    if (existing[0]?.exists) {
      throw new BadRequestException('Tax ID already exists');
    }
  }

  /**
   * 验证并检查文件是否为正确类型以及属于当前用户
   * @param fileId
   * @param ownerId
   * @param db
   * @private
   */
  private async validateAndCheckFiles(
    fileId: string,
    ownerId: string,
    db: DrizzleDb,
  ) {
    const validateFiles = await db
      .select({ mime_type: files.mime_type })
      .from(files)
      .innerJoin(user_uploads, eq(user_uploads.file_id, files.id))
      .where(
        and(eq(files.id, BigInt(fileId)), eq(user_uploads.user_id, ownerId)),
      )
      .limit(1);

    if (!validateFiles[0]) {
      throw new BadRequestException('File not found');
    }

    const mime_type = validateFiles[0].mime_type;

    if (!IMAGE_MIME_TYPES.has(mime_type)) {
      throw new BadRequestException(`Invalid file mime type: ${mime_type}`);
    }
  }

  async updateWholesalerProfile(
    id: string,
    dto: IUpdateWholesalerProfileDto,
    ability: AppAbility,
  ) {
    // 只有批发商本人可以更新主页
    if (!ability.can(Action.Update, 'users', id)) {
      throw new ForbiddenException(
        'You are not allowed to update this profile',
      );
    }
    const {
      telephone,
      username,
      tax_id,
      first_name,
      last_name,
      ...newProfile
    } = dto;

    return this.drizzle.db.transaction(async (tx) => {
      const profile = await tx
        .select({ profile: users.profile })
        .from(users)
        .where(eq(users.id, id))
        .for('update');

      if (!profile[0]) {
        throw new ForbiddenException('User not found');
      }

      // 检查 LOGO 是否为图片并属于用户
      if (newProfile.logo_file_id) {
        await this.validateAndCheckFiles(newProfile.logo_file_id, id, tx);
      }

      // 更改税号前先检查是否是唯一的 WHOLESALER 税号
      if (tax_id !== null && tax_id !== undefined) {
        await this.checkWholesalerTaxId(id, tax_id, tx);
      }

      const wholesalerProfile = profile[0]?.profile as IWholesalerProfile;
      const mergedProfile = buildMergedUpdate(wholesalerProfile, newProfile);

      await tx
        .update(users)
        .set({
          profile: mergedProfile,
          tax_id,
          telephone,
          username,
          first_name,
          last_name,
          updated_by: id,
          updated_at: sql`(NOW() AT TIME ZONE 'UTC')`,
        })
        .where(eq(users.id, id));
    });
  }

  async getWholesalerProfile(id: string, ability: AppAbility) {
    if (!ability.can(Action.Read, 'users', id)) {
      throw new ForbiddenException('You are not allowed to read this profile');
    }

    const wholesalerProfile = await this.drizzle.db.query.users.findFirst({
      where: eq(users.id, id),
      columns: {
        email: true,
        user_id: true,
        first_name: true,
        last_name: true,
        telephone: true,
        username: true,
        tax_id: true,
        profile: true,
      },
    });

    if (!wholesalerProfile) {
      throw new NotFoundException('User not found');
    }

    return {
      email: wholesalerProfile.email,
      user_id: wholesalerProfile.user_id,
      first_name: wholesalerProfile.first_name,
      last_name: wholesalerProfile.last_name,
      username: wholesalerProfile.username,
      telephone: wholesalerProfile.telephone,
      tax_id: wholesalerProfile.tax_id,
      profile: wholesalerProfile.profile as IWholesalerProfile,
    };
  }
}
