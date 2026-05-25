import {
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { subject } from '@casl/ability';
import { and, eq } from 'drizzle-orm';

import { Action } from '#/casl/actions.js';
import type { AppAbility } from '#/casl/casl-types.js';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import { SQL_NOW } from '#/drizzle/drizzle.constants.js';
import { AddressType, UserRole } from '#/generated/drizzle/enums.js';
import { directions, users } from '#/generated/drizzle/schema.js';
import type { IUpdateRetailerProfileDto } from '#/user/dto/update-retailer-profile.dto.js';
import type { RetailerProfileResponseDto } from '#/user/dto/retailer-profile-response.dto.js';
import type { IRetailerProfile } from '#/user/type/retailer-profile.type.js';
import { buildMergedUpdate } from '#/utils/patch.utils.js';
import {
  checkUserTaxId,
  validateAndCheckUserFiles,
} from '#/utils/db/user.db.utils.js';
import { restoreFilesFromCleanup } from '#/utils/db/file.db.utils.js';
import { PinoLogger } from 'nestjs-pino';
import { checkAddressIsValidForPatch } from '#/utils/db/address.db.utils.js';

@Injectable()
export class RetailerProfileService {
  constructor(
    private readonly drizzle: DrizzleService,
    private readonly logger: PinoLogger,
  ) {
    this.logger.setContext(RetailerProfileService.name);
  }

  async updateRetailerProfile(
    userId: string,
    dto: IUpdateRetailerProfileDto,
    ability: AppAbility,
  ) {
    // 检查权限：批发商可以访问任何零售商信息，零售商只能访问自己的信息
    if (!ability.can(Action.Read, subject('users', { id: userId }))) {
      // 如果基于ID的检查失败，尝试基于角色的权限检查
      if (
        !ability.can(Action.Read, subject('users', { role: UserRole.RETAILER }))
      ) {
        throw new ForbiddenException(
          'You are not allowed to read this profile',
        );
      }
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
      const [retailer] = await tx
        .select({
          profile: users.profile,
          status: users.status,
        })
        .from(users)
        .where(and(eq(users.id, userId), eq(users.role, UserRole.RETAILER)))
        .for('update');

      if (!retailer) {
        throw new ForbiddenException('User not found');
      }

      if (imageFileId) {
        await validateAndCheckUserFiles(imageFileId.toString(), userId, tx);
        await restoreFilesFromCleanup([imageFileId], tx);
      }

      if (tax_id !== null && tax_id !== undefined) {
        await checkUserTaxId(userId, tax_id, UserRole.RETAILER, tx);
      }

      const retailerProfile = (retailer.profile ?? {}) as IRetailerProfile;
      const mergedProfile = buildMergedUpdate(retailerProfile, newProfile);

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
          updated_by: userId,
          updated_at: SQL_NOW,
        })
        .where(eq(users.id, userId))
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
            country_iso: countryIso,
            province_id: provinceId,
            city_id: cityId,
            street: dto.address?.street,
            zip_code: dto.address?.zipCode,
            latitude: dto.address?.latitude,
            longitude: dto.address?.longitude,
            updated_at: SQL_NOW,
          })
          .where(eq(directions.id, addressId))
          .returning({
            user_id: directions.user_id,
          });

        if (!updatedAddress) {
          this.logger.error('user address not found', { userId });
          throw new NotFoundException('User address not found');
        }
      }
    });
  }

  async getRetailerProfile(
    id: string,
    ability: AppAbility,
  ): Promise<RetailerProfileResponseDto> {
    if (!ability.can(Action.Read, subject('users', { id }))) {
      throw new ForbiddenException('You are not allowed to read this profile');
    }

    const retailerProfile = await this.drizzle.db.query.users.findFirst({
      where: and(eq(users.id, id), eq(users.role, UserRole.RETAILER)),
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

    if (!retailerProfile) {
      throw new NotFoundException('User not found');
    }

    return {
      id: retailerProfile.id,
      email: retailerProfile.email,
      user_id: retailerProfile.user_id,
      first_name: retailerProfile.first_name,
      profile_image_file_id: retailerProfile.profile_image_file_id,
      last_name: retailerProfile.last_name,
      username: retailerProfile.username,
      telephone: retailerProfile.telephone,
      tax_id: retailerProfile.tax_id,
      profile: retailerProfile.profile as IRetailerProfile | null,
      store_directions: retailerProfile.directions[0] ?? null,
    };
  }
}
