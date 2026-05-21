import {
  ConflictException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { randomUUID } from 'node:crypto';
import { AUTH_ERROR, VerificationEmailType } from '../auth.constants.js';
import { VerificationService } from './verification.service.js';
import { IVerifyCodeDto } from '../dto/verification.dto.js';
import { IRegisterRetailerDto } from '../dto/register-retailer.dto.js';
import { HashService } from '#/common/hash/hash.service.js';
import { ISendNormalRegisterMailDto } from '../dto/register.dto.js';
import { IRegisterWholesalerDto } from '../dto/register-wholesaler.dto.js';
import { IWholesalerProfile } from '#/enterprise/types/IWholesalerProfile.js';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import {
  configurations,
  directions,
  users,
} from '#/generated/drizzle/schema.js';
import { and, eq, sql } from 'drizzle-orm';
import {
  AddressType,
  UserRole,
  UserStatus,
} from '#/generated/drizzle/enums.js';
import { checkAddressIsValid } from '#/utils/db/address.db.utils.js';

@Injectable()
export class RegistrationService {
  constructor(
    private readonly drizzleService: DrizzleService,
    private readonly hashService: HashService,
    private readonly verificationService: VerificationService,
  ) {}

  private async beginNormalRegistration(
    dto: ISendNormalRegisterMailDto,
    role: UserRole,
  ) {
    await this.drizzleService.db.transaction(async (tx) => {
      const [user] = await tx
        .select({ status: users.status })
        .from(users)
        .where(eq(users.email, dto.email));

      if (user) {
        if (user.status !== UserStatus.PENDING_VERIFICATION) {
          throw new ConflictException(AUTH_ERROR.EMAIL_CONFLICT);
        } else {
          const updatedUser = tx
            .$with('updated_user')
            .as(
              tx
                .update(users)
                .set({ role: role })
                .where(eq(users.email, dto.email))
                .returning({ id: users.id }),
            );
          await tx
            .with(updatedUser)
            .update(configurations)
            .set({ language: dto.language, timezone: dto.timezone })
            .where(
              eq(configurations.user_id, sql`(SELECT id FROM updated_user)`),
            );
        }
      } else {
        const createdUser = tx.$with('created_user').as(
          tx
            .insert(users)
            .values({
              email: dto.email,
              password: randomUUID(),
              role: role,
            })
            .returning({ id: users.id }),
        );
        await tx
          .with(createdUser)
          .insert(configurations)
          .values({
            user_id: sql`(SELECT id FROM created_user)`,
            language: dto.language,
            timezone: dto.timezone,
          });
      }
    });

    await this.verificationService.sendVerificationCode(
      { email: dto.email, deepLink: dto.deepLink ?? '' },
      VerificationEmailType.NORMAL_REGISTER,
    );
  }

  async beginRetailerRegistration(dto: ISendNormalRegisterMailDto) {
    return this.beginNormalRegistration(dto, UserRole.RETAILER);
  }

  async completeRetailerRegistration(dto: IRegisterRetailerDto) {
    return this.drizzleService.db.transaction(async (tx) => {
      await checkAddressIsValid(
        dto.address.city,
        dto.address.province,
        dto.address.country,
        tx,
      );

      await this.verificationService.verifyAndConsumeToken(
        tx,
        dto.verification_id,
        dto.token,
      );

      const hashedPassword = await this.hashService.hashWithBcrypt(
        dto.password,
      );

      // CTE 1: 更新用户并返回 id
      const updatedUserCte = tx.$with('updated_user').as(
        tx
          .update(users)
          .set({
            status: UserStatus.ACTIVE,
            username: dto.username ?? randomUUID(),
            password: hashedPassword,
          })
          .where(
            and(
              eq(users.email, dto.email),
              eq(users.status, UserStatus.PENDING_VERIFICATION),
            ),
          )
          .returning({ id: users.id }),
      );
      // CTE 2: 插入地址，使用 CTE 1 中的 id
      const result = await tx
        .with(updatedUserCte)
        .insert(directions)
        .values({
          user_id: sql`(SELECT id FROM updated_user)`,
          country_iso: dto.address.country,
          province_id: dto.address.province,
          city_id: dto.address.city,
          street: dto.address.street,
          type: AddressType.STORE,
          zip_code: dto.address.zipCode,
          latitude: dto.address.latitude,
          longitude: dto.address.longitude,
        });

      if (!result.rowCount || result.rowCount < 1) {
        throw new NotFoundException(AUTH_ERROR.USER_NOT_FOUND);
      }
    });
  }

  async beginWholesalerRegistration(dto: ISendNormalRegisterMailDto) {
    return this.beginNormalRegistration(dto, UserRole.WHOLESALER);
  }

  async completeWholesalerRegistration(dto: IRegisterWholesalerDto) {
    await this.drizzleService.db.transaction(async (tx) => {
      await checkAddressIsValid(
        dto.address.city,
        dto.address.province,
        dto.address.country,
        tx,
      );

      await this.verificationService.verifyAndConsumeToken(
        tx,
        dto.verification_id,
        dto.token,
      );

      const hashedPassword = await this.hashService.hashWithBcrypt(
        dto.password,
      );

      const wholesalerProfile: IWholesalerProfile = {
        company_name: dto.company_name,
        company_type: dto.company_type,
      };

      const updatedUser = await tx
        .with(
          tx.$with('updated_user').as(
            tx
              .update(users)
              .set({
                status: UserStatus.ACTIVE,
                username: dto.username ?? randomUUID(),
                password: hashedPassword,
                telephone: dto.telephone,
                role: UserRole.WHOLESALER,
                profile: wholesalerProfile,
              })
              .where(
                and(
                  eq(users.email, dto.email),
                  eq(users.status, UserStatus.PENDING_VERIFICATION),
                ),
              )
              .returning({ id: users.id }),
          ),
        )
        .insert(directions)
        .values({
          user_id: sql`(SELECT id FROM updated_user)`,
          country_iso: dto.address.country,
          province_id: dto.address.province,
          city_id: dto.address.city,
          street: dto.address.street,
          type: AddressType.STORE,
          zip_code: dto.address.zipCode,
          latitude: dto.address.latitude,
          longitude: dto.address.longitude,
        });

      if (!updatedUser.rowCount || updatedUser.rowCount < 1) {
        throw new NotFoundException(AUTH_ERROR.USER_NOT_FOUND);
      }
    });
  }

  async verifyCode(verifyCodeDto: IVerifyCodeDto) {
    return this.verificationService.verifyCode(verifyCodeDto, 30);
  }
}
