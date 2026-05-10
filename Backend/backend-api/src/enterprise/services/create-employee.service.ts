import { ForbiddenException, Injectable } from '@nestjs/common';
import { randomUUID } from 'node:crypto';
import { ICreateEmployeeDto } from '../dto/create-employee.dto.js';
import { AUTH_ERROR, AUTH_VERIFY_EMAIL_PATH } from '#/auth/auth.constants.js';
import { HashService } from '#/common/hash/hash.service.js';
import { addDays } from '#/utils/date.utils.js';
import { MailService } from '#/mail/mail.service.js';
import { IWholesalerProfile } from '#/enterprise/types/IWholesalerProfile.js';
import { RoleI18nService } from '#/common/i18n/role.i18n.js';
import { UserRole } from '#/generated/drizzle/enums.js';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import { eq } from 'drizzle-orm';
import {
  configurations,
  users,
  verification_tokens,
} from '#/generated/drizzle/schema.js';

@Injectable()
export class CreateEmployeeService {
  constructor(
    //private readonly prismaService: PrismaService,
    private readonly drizzleService: DrizzleService,
    private readonly hashService: HashService,
    private readonly mailService: MailService,
    private readonly roleI18nService: RoleI18nService,
  ) {}

  /*
  async createEmployee(
    wholesalerId: string,
    dto: ICreateEmployeeDto,
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
    const wholesalerData = result.profile as unknown as IWholesalerProfile;
    const position = this.roleI18nService.translateRole(userRole, result.lang);

    await this.mailService.sendEmployeeVerifyEmail({
      to: dto.email,
      lang: result.lang,
      link: link,
      companyName: wholesalerData.company_name,
      position: position,
    });
  }*/

  async createEmployee(
    wholesalerId: string,
    dto: ICreateEmployeeDto,
    userRole: UserRole,
  ) {
    const result = await this.drizzleService.db.transaction(async (tx) => {
      const wholesaler = await tx.query.users.findFirst({
        where: eq(users.id, wholesalerId),
        columns: {
          user_id: true,
          profile: true,
        },
        with: {
          configurations: { columns: { language: true } },
        },
      });

      if (!wholesaler) {
        throw new ForbiddenException(AUTH_ERROR.ACCESS_DENIED);
      }

      const employeeUsername = `${wholesaler.user_id}@${dto.username ?? randomUUID()}`;

      const [employee] = await tx
        .insert(users)
        .values({
          email: dto.email,
          role: userRole,
          username: employeeUsername,
          password: '',
          telephone: dto.telephone,
          tax_id: dto.cif,
          first_name: dto.first_name,
          last_name: dto.last_name,
        })
        .onConflictDoUpdate({
          target: [users.email, users.username],
          setWhere: eq(users.status, 'PENDING_VERIFICATION'),
          set: { status: 'PENDING_VERIFICATION' },
        })
        .returning({ id: users.id });

      const token = `${wholesaler.user_id}@${randomUUID()}`;
      const hashedToken = await this.hashService.hashWithCrypto(token);

      await tx.insert(verification_tokens).values({
        user_id: employee.id,
        token: hashedToken,
        expires_at: addDays(new Date(), 7).toISOString(),
      });

      await tx.insert(configurations).values({
        user_id: employee.id,
        language: wholesaler.configurations[0]?.language,
      });

      return {
        employeeId: employee.id,
        token,
        lang: wholesaler.configurations[0]?.language,
        profile: wholesaler.profile,
      };
    });

    const link = `${AUTH_VERIFY_EMAIL_PATH}?token=${result.token}&userId=${result.employeeId}&lang=${result.lang}`;
    const wholesalerData = result.profile as IWholesalerProfile;
    const position = this.roleI18nService.translateRole(userRole, result.lang);

    await this.mailService.sendEmployeeVerifyEmail({
      to: dto.email,
      lang: result.lang,
      link: link,
      companyName: wholesalerData.company_name,
      position: position,
    });
  }

  async createSupportEmployee(wholesalerId: string, dto: ICreateEmployeeDto) {
    return this.createEmployee(wholesalerId, dto, UserRole.SUPPORT);
  }

  async createDeliveryEmployee(wholesalerId: string, dto: ICreateEmployeeDto) {
    return this.createEmployee(wholesalerId, dto, UserRole.DELIVERY);
  }

  async createWarehouseEmployee(wholesalerId: string, dto: ICreateEmployeeDto) {
    return this.createEmployee(wholesalerId, dto, UserRole.WAREHOUSE);
  }
}
