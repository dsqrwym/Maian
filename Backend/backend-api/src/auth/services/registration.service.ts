import {
  ConflictException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { PrismaService } from 'src/prisma/prisma.service';
import { $Enums, AddressType, UserRole } from 'src/generated/prisma/client';
import { randomUUID } from 'node:crypto';
import { AUTH_ERROR, VerificationEmailType } from '../auth.constants';
import { VerificationService } from './verification.service';
import { IVerifyCodeDto } from '../dto/verification.dto';
import { IRegisterRetailerDto } from '../dto/register-retailer.dto';
import UserStatus = $Enums.UserStatus;
import { HashService } from 'src/common/hash/hash.service';
import { ISendNormalRegisterMailDto } from '../dto/register.dto';
import { IRegisterWholesalerDto } from '../dto/register-wholesaler.dto';
import { WholesalerProfileType } from '../../enterprise/types/wholesaler-profile.type';

@Injectable()
export class RegistrationService {
  constructor(
    private readonly prismaService: PrismaService,
    private readonly hashService: HashService,
    private readonly verificationService: VerificationService,
  ) {}

  private async beginNormalRegistration(
    dto: ISendNormalRegisterMailDto,
    role: UserRole,
  ) {
    await this.prismaService.$transaction(async (tx) => {
      const user = await tx.users.findUnique({
        where: { email: dto.email },
        select: { status: true },
      });

      if (user) {
        if (user.status !== UserStatus.PENDING_VERIFICATION) {
          throw new ConflictException(AUTH_ERROR.EMAIL_CONFLICT);
        } else {
          await tx.users.update({
            where: { email: dto.email },
            data: {
              role: role,
              configurations: {
                update: { language: dto.language, timezone: dto.timezone },
              },
            },
          });
        }
      } else {
        await tx.users.create({
          data: {
            email: dto.email,
            password: randomUUID(),
            role: role,
            configurations: {
              create: { language: dto.language, timezone: dto.timezone },
            },
          },
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
    await this.prismaService.$transaction(async (tx) => {
      await this.verificationService.verifyAndConsumeToken(
        tx,
        dto.verification_id,
        dto.token,
      );

      const hashedPassword = await this.hashService.hashWithBcrypt(
        dto.password,
      );

      const updatedUser = await tx.users.update({
        select: { id: true },
        where: { email: dto.email, status: UserStatus.PENDING_VERIFICATION },
        data: {
          status: UserStatus.ACTIVE,
          username: dto.username ?? randomUUID(),
          password: hashedPassword,
          directions: {
            create: {
              country_iso: dto.address.country,
              province_id: dto.address.province,
              city_id: dto.address.city,
              street: dto.address.street,
              type: AddressType.STORE,
              zip_code: dto.address.zipCode,
              latitude: dto.address.latitude,
              longitude: dto.address.longitude,
            },
          },
        },
      });
      if (!updatedUser) {
        throw new NotFoundException(AUTH_ERROR.USER_NOT_FOUND);
      }
    });
  }

  async beginWholesalerRegistration(dto: ISendNormalRegisterMailDto) {
    return this.beginNormalRegistration(dto, UserRole.WHOLESALER);
  }

  async completeWholesalerRegistration(dto: IRegisterWholesalerDto) {
    await this.prismaService.$transaction(async (tx) => {
      await this.verificationService.verifyAndConsumeToken(
        tx,
        dto.verification_id,
        dto.token,
      );

      const hashedPassword = await this.hashService.hashWithBcrypt(
        dto.password,
      );

      const wholesalerProfile: WholesalerProfileType = {
        company_name: dto.company_name,
        company_type: dto.company_type,
      };

      const updatedUser = await tx.users.update({
        select: { id: true },
        where: { email: dto.email, status: UserStatus.PENDING_VERIFICATION },
        data: {
          status: UserStatus.ACTIVE,
          username: dto.username ?? randomUUID(),
          password: hashedPassword,
          telephone: dto.telephone,
          role: UserRole.WHOLESALER,
          profile: wholesalerProfile,
          directions: {
            create: {
              country_iso: dto.address.country,
              province_id: dto.address.province,
              city_id: dto.address.city,
              street: dto.address.street,
              type: AddressType.STORE,
              zip_code: dto.address.zipCode,
              latitude: dto.address.latitude,
              longitude: dto.address.longitude,
            },
          },
        },
      });
      if (!updatedUser) {
        throw new NotFoundException(AUTH_ERROR.USER_NOT_FOUND);
      }
    });
  }

  async verifyCode(verifyCodeDto: IVerifyCodeDto) {
    return this.verificationService.verifyCode(verifyCodeDto, 30);
  }
}
