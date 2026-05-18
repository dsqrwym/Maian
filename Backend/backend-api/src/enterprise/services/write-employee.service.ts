import {
  ConflictException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
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
import { and, eq, exists, sql } from 'drizzle-orm';
import {
  configurations,
  users,
  verification_tokens,
  wholesaler_staffs,
} from '#/generated/drizzle/schema.js';
import { IUpdateEmployeeDto } from '#/enterprise/dto/update-employee.dto.js';
import { TagsUuid } from '#/utils/typia/validators/auth.validator.js';

@Injectable()
export class WriteEmployeeService {
  constructor(
    private readonly drizzleService: DrizzleService,
    private readonly hashService: HashService,
    private readonly mailService: MailService,
    private readonly roleI18nService: RoleI18nService,
  ) {}

  async deleteEmployee(id: TagsUuid, wholesalerId: string) {
    const [employee] = await this.drizzleService.db
      .delete(users)
      .where(
        and(
          eq(users.id, id),
          exists(
            this.drizzleService.db
              .select({ one: sql`1` })
              .from(wholesaler_staffs)
              .where(
                and(
                  eq(wholesaler_staffs.staff_user_id, id),
                  eq(wholesaler_staffs.wholesaler_id, wholesalerId),
                ),
              ),
          ),
        ),
      )
      .returning({ id: users.id });

    if (!employee) {
      throw new NotFoundException('Employee not found');
    }
  }

  async updateEmployee(
    employeeId: string,
    wholesalerId: string,
    dto: IUpdateEmployeeDto,
  ) {
    const { first_name, last_name, username, telephone, tax_id } = dto;

    await this.drizzleService.db.transaction(async (tx) => {
      const [employee] = await tx
        .update(users)
        .set({
          first_name,
          last_name,
          username,
          telephone,
          tax_id,
        })
        .where(
          and(
            eq(users.id, employeeId),
            exists(
              tx
                .select({ one: sql`1` })
                .from(wholesaler_staffs)
                .where(
                  and(
                    eq(wholesaler_staffs.staff_user_id, employeeId),
                    eq(wholesaler_staffs.wholesaler_id, wholesalerId),
                  ),
                ),
            ),
          ),
        )
        .returning({ id: users.id });
      if (!employee) {
        throw new NotFoundException('Employee not found');
      }
    });
  }

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
          tax_id: dto.tax_id,
          first_name: dto.first_name,
          last_name: dto.last_name,
        })
        .onConflictDoUpdate({
          target: [users.email],
          setWhere: eq(users.status, 'PENDING_VERIFICATION'),
          set: { status: 'PENDING_VERIFICATION' },
        })
        .returning({ id: users.id })
        .catch(() => {
          throw new ConflictException('Username already used');
        });

      await tx
        .insert(wholesaler_staffs)
        .values({
          wholesaler_id: wholesalerId,
          staff_user_id: employee.id,
          role: userRole,
        })
        .onConflictDoNothing();

      const token = `${wholesaler.user_id}@${randomUUID()}`;
      const hashedToken = await this.hashService.hashWithCrypto(token);

      await tx.insert(verification_tokens).values({
        user_id: employee.id,
        token: hashedToken,
        expires_at: addDays(new Date(), 7).toISOString(),
      });

      const language = wholesaler.configurations[0]?.language ?? 'en';

      await tx
        .insert(configurations)
        .values({
          user_id: employee.id,
          language: language,
        })
        .onConflictDoNothing();

      return {
        employeeId: employee.id,
        token,
        lang: language,
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
