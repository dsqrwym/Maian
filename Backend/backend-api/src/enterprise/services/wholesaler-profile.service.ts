import { DrizzleDb, DrizzleService } from '#/drizzle/drizzle.service.js';
import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { AppAbility } from '#/casl/casl-types.js';
import { Action } from '#/casl/actions.js';
import {
  directions,
  files,
  user_uploads,
  users,
} from '#/generated/drizzle/schema.js';
import { and, eq, exists, ne } from 'drizzle-orm';
import { IWholesalerProfile } from '#/enterprise/types/IWholesalerProfile.js';
import { buildMergedUpdate } from '#/utils/patch.utils.js';
import { IMAGE_MIME_TYPES } from '#/config/fastify-multipart.config.js';
import { AddressType, UserRole } from '#/generated/drizzle/enums.js';
import { IUpdateWholesalerProfileDto } from '#/enterprise/dto/update-wholesaler-profile.dto.js';
import { SQL_NOW, SQL_TEMP_TABLE } from '#/drizzle/drizzle.constants.js';

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
      .from(SQL_TEMP_TABLE)
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
    const [validateFiles] = await db
      .select({ mime_type: files.mime_type })
      .from(files)
      .innerJoin(user_uploads, eq(user_uploads.file_id, files.id))
      .where(
        and(eq(files.id, BigInt(fileId)), eq(user_uploads.user_id, ownerId)),
      )
      .limit(1);

    if (!validateFiles) {
      throw new BadRequestException('File not found');
    }

    const mime_type = validateFiles.mime_type;

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
      profile_image_file_id,
      ...newProfile
    } = dto;
    const imageFileId =
      profile_image_file_id === undefined
        ? undefined
        : profile_image_file_id === null
          ? null
          : BigInt(profile_image_file_id);

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
      if (profile_image_file_id) {
        await this.validateAndCheckFiles(profile_image_file_id, id, tx);
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
          profile_image_file_id: imageFileId,
          tax_id,
          telephone,
          username,
          first_name,
          last_name,
          updated_by: id,
          updated_at: SQL_NOW,
        })
        .where(eq(users.id, id));
    });
  }

  async getWholesalerProfile(id: string, ability: AppAbility) {
    if (!ability.can(Action.Read, 'users', id)) {
      throw new ForbiddenException('You are not allowed to read this profile');
    }

    const wholesalerProfile = await this.drizzle.db.query.users.findFirst({
      where: and(eq(users.id, id), eq(users.role, UserRole.WHOLESALER)),
      columns: {
        id: true,
        email: true,
        user_id: true,
        first_name: true,
        profile_image_file_id: true,
        last_name: true,
        telephone: true,
        username: true,
        profile: true,
        tax_id: true,
      },
      with: {
        directions: {
          columns: {
            street: true,
            zip_code: true,
          },
          with: {
            province: { columns: { name: true, name_local: true, id: true } },
            city: { columns: { name: true, name_local: true, id: true } },
            country: {
              columns: { name: true, name_local: true, iso_numeric: true },
            },
          },
          where: eq(directions.type, AddressType.STORE),
        },
      },
    });

    if (!wholesalerProfile) {
      throw new NotFoundException('User not found');
    }

    return {
      id: wholesalerProfile.id,
      email: wholesalerProfile.email,
      user_id: wholesalerProfile.user_id,
      first_name: wholesalerProfile.first_name,
      profile_image_file_id: wholesalerProfile.profile_image_file_id,
      last_name: wholesalerProfile.last_name,
      username: wholesalerProfile.username,
      telephone: wholesalerProfile.telephone,
      tax_id: wholesalerProfile.tax_id,
      profile: wholesalerProfile.profile as IWholesalerProfile,
      store_directions: wholesalerProfile.directions[0] ?? null,
    };
  }
}
