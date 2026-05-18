import { Injectable, ServiceUnavailableException } from '@nestjs/common';
import { eq } from 'drizzle-orm';

import { DrizzleService } from '#/drizzle/drizzle.service.js';
import { configurations } from '#/generated/drizzle/schema.js';
import type { IUpdateUserLanguageDto } from '#/user/dto/update-user-language.dto.js';
import type { UserSettingsResponseDto } from '#/user/dto/user-settings-response.dto.js';

@Injectable()
export class UserSettingsService {
  constructor(private readonly drizzle: DrizzleService) {}

  async getSettings(userId: string): Promise<UserSettingsResponseDto> {
    const settings = await this.findSettings(userId);
    if (settings) return settings;

    const [created] = await this.drizzle.db
      .insert(configurations)
      .values({ user_id: userId })
      .onConflictDoNothing()
      .returning({
        language: configurations.language,
        timezone: configurations.timezone,
      });

    if (created) return created;

    const existing = await this.findSettings(userId);
    if (existing) return existing;

    throw new ServiceUnavailableException('Failed to load user settings');
  }

  async updateLanguage(
    userId: string,
    dto: IUpdateUserLanguageDto,
  ): Promise<void> {
    const [settings] = await this.drizzle.db
      .insert(configurations)
      .values({
        user_id: userId,
        language: dto.language,
      })
      .onConflictDoUpdate({
        target: configurations.user_id,
        set: {
          language: dto.language,
        },
      })
      .returning({
        language: configurations.language,
        timezone: configurations.timezone,
      });

    if (!settings) {
      throw new ServiceUnavailableException('Failed to update user language');
    }
  }

  private async findSettings(userId: string): Promise<UserSettingsResponseDto> {
    const [settings] = await this.drizzle.db
      .select({
        language: configurations.language,
        timezone: configurations.timezone,
      })
      .from(configurations)
      .where(eq(configurations.user_id, userId))
      .limit(1);

    return settings;
  }
}
