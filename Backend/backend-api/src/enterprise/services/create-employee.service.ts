import { ForbiddenException, Injectable } from '@nestjs/common';
import { randomUUID } from 'node:crypto';
import { PrismaService } from '../../prisma/prisma.service';
import { CreateEmployeeDto } from '../dto/create-employee.dto';
import { $Enums, UserRole } from '../../../prisma/generated';
import { AUTH_ERROR, AUTH_VERIFY_EMAIL_PATH } from '../../auth/auth.constants';
import { HashService } from '../../common/hash/hash.service';
import { addDays } from '../../utils/date.utils';
import { MailService } from '../../mail/mail.service';
import { WholesalerProfileType } from '../types/wholesaler-profile.type';
import UserStatus = $Enums.UserStatus;
import { RoleI18nService } from 'src/common/i18n/role.i18n';

@Injectable()
export class CreateEmployeeService {
  constructor(
    private readonly prismaService: PrismaService,
    private readonly hashService: HashService,
    private readonly mailService: MailService,
    private readonly roleI18nService: RoleI18nService,
  ) {}

  async createEmployee(
    wholesalerId: string,
    dto: CreateEmployeeDto,
    userRole: UserRole,
  ) {
    const result = await this.prismaService.$transaction(async (tx) => {
      const wholesaler = await tx.users.findUnique({
        where: { id: wholesalerId },
        select: {
          user_id: true,
          profile: true,
          configurations: { select: { language: true } },
        },
      });

      if (!wholesaler) {
        throw new ForbiddenException(AUTH_ERROR.ACCESS_DENIED);
      }

      const employeeUsername = `${wholesaler.user_id}@${dto.username ?? randomUUID()}`;

      const employee = await tx.users.upsert({
        where: {
          email: dto.email,
          username: employeeUsername,
          status: UserStatus.PENDING_VERIFICATION,
        },
        create: {
          email: dto.email,
          role: userRole,
          username: employeeUsername,
          password: '',
          telephone: dto.telephone,
          cif: dto.cif,
          first_name: dto.first_name,
          last_name: dto.last_name,
        },
        update: {},
        select: { id: true },
      });

      const token = `${wholesaler.user_id}@${randomUUID()}`;
      const hashedToken = await this.hashService.hashWithCrypto(token);

      await tx.verification_tokens.create({
        data: {
          user_id: employee.id,
          token: hashedToken,
          expires_at: addDays(new Date(), 7),
        },
      });

      await tx.configurations.create({
        data: {
          user_id: employee.id,
          language: wholesaler.configurations?.language,
        },
      });

      return {
        employeeId: employee.id,
        token,
        lang: wholesaler.configurations?.language,
        profile: wholesaler.profile,
      };
    });

    const link = `${AUTH_VERIFY_EMAIL_PATH}?token=${result.token}&userId=${result.employeeId}&lang=${result.lang}`;
    const wholesalerData = result.profile as unknown as WholesalerProfileType;
    const position = this.roleI18nService.translateRole(userRole, result.lang);

    await this.mailService.sendEmployeeVerifyEmail({
      to: dto.email,
      lang: result.lang,
      link: link,
      companyName: wholesalerData.company_name,
      position: position,
    });
  }

  async createSupportEmployee(wholesalerId: string, dto: CreateEmployeeDto) {
    return this.createEmployee(wholesalerId, dto, UserRole.SUPPORT);
  }

  async createDeliveryEmployee(wholesalerId: string, dto: CreateEmployeeDto) {
    return this.createEmployee(wholesalerId, dto, UserRole.DELIVERY);
  }

  async createWarehouseEmployee(wholesalerId: string, dto: CreateEmployeeDto) {
    return this.createEmployee(wholesalerId, dto, UserRole.WAREHOUSE);
  }
}
