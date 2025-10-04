import {
  ConflictException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { PrismaService } from 'src/prisma/prisma.service';
import { $Enums, AddressType, UserRole } from 'prisma/generated';
import { randomUUID } from 'node:crypto';
import { AUTH_ERROR } from '../auth.constants';
import { VerificationService } from './verification.service';
import { VerifyCodeDto } from '../dto/verification.dto';
import { VerificationEmailType } from '../auth.types';
import { RegisterRetailerDto } from '../dto/register-retailer.dto';
import UserStatus = $Enums.UserStatus;
import { HashService } from 'src/common/hash/hash.service';
import { SendNormalRegisterMailDto } from '../dto/register.dto';
import { RegisterWholesalerDto } from '../dto/register-wholesaler.dto';
import { WholesalerProfileType } from '../../user/type/wholesaler-profile.type';

@Injectable()
export class RegistrationService {
  constructor(
    private readonly prismaService: PrismaService,
    private readonly hashService: HashService,
    private readonly verificationService: VerificationService,
  ) {}

  private async beginNormalRegistration(
    dto: SendNormalRegisterMailDto,
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
      dto,
      VerificationEmailType.NORMAL_REGISTER,
    );
  }
  async beginRetailerRegistration(dto: SendNormalRegisterMailDto) {
    return this.beginNormalRegistration(dto, UserRole.RETAILER);
  }

  async completeRetailerRegistration(dto: RegisterRetailerDto) {
    await this.prismaService.$transaction(async (tx) => {
      await this.verificationService.verifyAndConsumeToken(
        tx,
        dto.verification_id,
        dto.token,
      );

      const hashedPassword = await this.hashService.hashWithCrypto(
        dto.password,
      );

      const updatedUser = await tx.users.update({
        select: { id: true },
        where: { email: dto.email, status: UserStatus.PENDING_VERIFICATION },
        data: {
          status: UserStatus.INACTIVE,
          username: dto.username ?? '',
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

  async beginWholesalerRegistration(dto: SendNormalRegisterMailDto) {
    return this.beginNormalRegistration(dto, UserRole.WHOLESALER);
  }

  async completeWholesalerRegistration(dto: RegisterWholesalerDto) {
    await this.prismaService.$transaction(async (tx) => {
      await this.verificationService.verifyAndConsumeToken(
        tx,
        dto.verification_id,
        dto.token,
      );

      const hashedPassword = await this.hashService.hashWithCrypto(
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
          status: UserStatus.INACTIVE,
          username: dto.username ?? '',
          password: hashedPassword,
          telephone: dto.telephone,
          role: UserRole.WHOLESALER,
          profile: JSON.stringify(wholesalerProfile),
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

  async verifyCode(verifyCodeDto: VerifyCodeDto) {
    return this.verificationService.verifyCode(verifyCodeDto, 24 * 60);
  }
}
