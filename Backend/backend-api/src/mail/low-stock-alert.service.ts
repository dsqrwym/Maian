import { Injectable } from '@nestjs/common';
import { PinoLogger } from 'nestjs-pino';
import { and, eq } from 'drizzle-orm';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import {
  configurations,
  users,
  wholesaler_staffs,
} from '#/generated/drizzle/schema.js';
import { UserRole, UserStatus } from '#/generated/drizzle/enums.js';
import { MailService } from '#/mail/mail.service.js';
import type { IWholesalerProfile } from '#/enterprise/types/IWholesalerProfile.js';
import type { LowStockAlertEmailItem } from '#/mail/mail.types.js';

export interface LowStockAlertState {
  previousAvailableStock: number;
  previousLowStockThreshold: number;
  currentAvailableStock: number;
  currentLowStockThreshold: number;
  previousStatus?: string;
  currentStatus?: string;
}

@Injectable()
export class LowStockAlertService {
  constructor(
    private readonly drizzle: DrizzleService,
    private readonly mailService: MailService,
    private readonly logger: PinoLogger,
  ) {
    this.logger.setContext(LowStockAlertService.name);
  }

  shouldTriggerLowStockAlert(state: LowStockAlertState): boolean {
    const wasAlertable =
      state.previousStatus === undefined || state.previousStatus === 'ACTIVE';
    const isAlertable =
      state.currentStatus === undefined || state.currentStatus === 'ACTIVE';
    const wasLow =
      wasAlertable &&
      state.previousLowStockThreshold > 0 &&
      state.previousAvailableStock <= state.previousLowStockThreshold;
    const isLow =
      isAlertable &&
      state.currentLowStockThreshold > 0 &&
      state.currentAvailableStock <= state.currentLowStockThreshold;

    return !wasLow && isLow;
  }

  async notifyLowStockAlerts(
    wholesalerId: string,
    items: LowStockAlertEmailItem[],
  ) {
    const uniqueItems = this.dedupeItems(items);
    if (uniqueItems.length === 0) return;

    const recipients = await this.getRecipients(wholesalerId);
    if (recipients.length === 0) {
      this.logger.warn(
        { wholesalerId, itemCount: uniqueItems.length },
        'No recipients found for low stock alert',
      );
      return;
    }

    await Promise.all(
      recipients.map((recipient) =>
        this.mailService.sendLowStockAlert({
          to: recipient.email,
          lang: recipient.language,
          companyName: recipient.companyName,
          items: uniqueItems,
        }),
      ),
    );
  }

  private dedupeItems(
    items: LowStockAlertEmailItem[],
  ): LowStockAlertEmailItem[] {
    const seen = new Set<string>();
    const result: LowStockAlertEmailItem[] = [];

    for (const item of items) {
      if (seen.has(item.variantProductId)) continue;
      seen.add(item.variantProductId);
      result.push(item);
    }

    return result;
  }

  private async getRecipients(wholesalerId: string) {
    const wholesaler = await this.drizzle.db.query.users.findFirst({
      where: eq(users.id, wholesalerId),
      columns: {
        email: true,
        profile: true,
      },
      with: {
        configurations: { columns: { language: true } },
      },
    });

    if (!wholesaler) return [];

    const wholesalerProfile = wholesaler.profile as IWholesalerProfile;
    const companyName =
      wholesalerProfile.display_name ?? wholesalerProfile.company_name;
    const defaultLanguage = wholesaler.configurations[0]?.language ?? 'en';
    const recipients = new Map<
      string,
      { email: string; language: string; companyName: string }
    >();

    recipients.set(wholesaler.email.toLowerCase(), {
      email: wholesaler.email,
      language: defaultLanguage,
      companyName,
    });

    const warehouseStaff = await this.drizzle.db
      .select({
        email: users.email,
        language: configurations.language,
      })
      .from(wholesaler_staffs)
      .innerJoin(users, eq(users.id, wholesaler_staffs.staff_user_id))
      .leftJoin(configurations, eq(configurations.user_id, users.id))
      .where(
        and(
          eq(wholesaler_staffs.wholesaler_id, wholesalerId),
          eq(wholesaler_staffs.role, UserRole.WAREHOUSE),
          eq(users.status, UserStatus.APPROVED),
        ),
      );

    for (const staff of warehouseStaff) {
      recipients.set(staff.email.toLowerCase(), {
        email: staff.email,
        language: staff.language ?? defaultLanguage,
        companyName,
      });
    }

    return [...recipients.values()];
  }
}
