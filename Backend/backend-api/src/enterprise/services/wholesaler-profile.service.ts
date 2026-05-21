import { DrizzleService } from '#/drizzle/drizzle.service.js';
import {
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { AppAbility } from '#/casl/casl-types.js';
import { Action } from '#/casl/actions.js';
import { directions, users } from '#/generated/drizzle/schema.js';
import { and, eq } from 'drizzle-orm';
import { IWholesalerProfile } from '#/enterprise/types/IWholesalerProfile.js';
import { buildMergedUpdate } from '#/utils/patch.utils.js';
import { AddressType, UserRole } from '#/generated/drizzle/enums.js';
import { IUpdateWholesalerProfileDto } from '#/enterprise/dto/update-wholesaler-profile.dto.js';
import { SQL_NOW } from '#/drizzle/drizzle.constants.js';
import {
  checkUserTaxId,
  validateAndCheckUserFiles,
} from '#/utils/db/user.db.utils.js';
import { subject } from '@casl/ability';
import { PinoLogger } from 'nestjs-pino';
import { checkAddressIsValidForPatch } from '#/utils/db/address.db.utils.js';

@Injectable()
export class WholesalerProfileService {
  constructor(
    private readonly drizzle: DrizzleService,
    private readonly logger: PinoLogger,
  ) {
    this.logger.setContext(WholesalerProfileService.name);
  }

  async updateWholesalerProfile(
    id: string,
    dto: IUpdateWholesalerProfileDto,
    ability: AppAbility,
  ) {
    // 只有批发商本人可以更新主页
    if (!ability.can(Action.Update, subject('users', { id }))) {
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
      address,
      ...newProfile
    } = dto;
    const imageFileId =
      profile_image_file_id === undefined
        ? undefined
        : profile_image_file_id === null
          ? null
          : BigInt(profile_image_file_id);

    return this.drizzle.db.transaction(async (tx) => {
      const [profile] = await tx
        .select({ profile: users.profile })
        .from(users)
        .where(and(eq(users.id, id), eq(users.role, UserRole.WHOLESALER)))
        .for('update');

      if (!profile) {
        throw new ForbiddenException('User not found');
      }

      // 检查 LOGO 是否为图片并属于用户
      if (profile_image_file_id) {
        await validateAndCheckUserFiles(profile_image_file_id, id, tx);
      }

      // 更改税号前先检查是否是唯一的 WHOLESALER 税号
      if (tax_id !== null && tax_id !== undefined) {
        await checkUserTaxId(id, tax_id, UserRole.WHOLESALER, tx);
      }

      const wholesalerProfile = profile?.profile as IWholesalerProfile;
      const mergedProfile = buildMergedUpdate(wholesalerProfile, newProfile);

      const [updatedUser] = await tx
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
        .where(eq(users.id, id))
        .returning({
          id: users.id,
        });

      if (!updatedUser) {
        throw new NotFoundException('User not found');
      }

      if (address) {
        const { cityId, provinceId, countryIso, addressId } =
          await checkAddressIsValidForPatch(
            updatedUser.id,
            tx,
            address.city,
            address.province,
            address.country,
          );

        const [updatedAddress] = await tx
          .update(directions)
          .set({
            street: address.street,
            zip_code: address.zipCode,
            country_iso: countryIso,
            province_id: provinceId,
            city_id: cityId,
            updated_at: SQL_NOW,
            latitude: address.latitude,
            longitude: address.longitude,
          })
          .where(eq(directions.id, addressId))
          .returning({
            user_id: directions.user_id,
          });
        if (!updatedAddress) {
          this.logger.error('user address not found', { userId: id });
          throw new NotFoundException('User address not found');
        }
      }
    });
  }

  async getWholesalerProfile(id: string, ability: AppAbility) {
    // 检查权限：零售商可以访问任何批发商信息，批发商只能访问自己的信息
    // 首先尝试基于ID的权限检查（适用于批发商访问自己的情况）
    if (!ability.can(Action.Read, subject('users', { id }))) {
      // 如果基于ID的检查失败，尝试基于角色的权限检查（适用于零售商访问批发商的情况）
      if (
        !ability.can(
          Action.Read,
          subject('users', { role: UserRole.WHOLESALER }),
        )
      ) {
        throw new ForbiddenException(
          'You are not allowed to read this profile',
        );
      }
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
