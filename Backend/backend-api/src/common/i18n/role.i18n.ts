import { I18nService } from 'nestjs-i18n';
import { I18nTranslations } from '../../i18n/generated/i18n.generated';
import { Injectable } from '@nestjs/common';
import { UserRole } from 'src/generated/drizzle/enums';

@Injectable()
export class RoleI18nService {
  constructor(private readonly i18nService: I18nService<I18nTranslations>) {}

  /**
   * 根据 UserRole 返回对应的国际化职位名称
   * @param role 用户角色
   * @param language 语言代码（例如 'zh', 'es', 'en'）
   */
  public translateRole(role: UserRole, language?: string): string {
    const lang = language || 'en';
    return this.i18nService.translate(`roles.${role}`, { lang });
  }
}
