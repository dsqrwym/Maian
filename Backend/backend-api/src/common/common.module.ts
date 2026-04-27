import { Global, Module } from '@nestjs/common';
import { DateFormatService } from './formatter/date-format.service.js';
import { HashService } from './hash/hash.service.js';
import { Bcp47LanguageValidator } from './validators/is-bcp47-language.validator.js';
import { IanaTimezoneValidator } from './validators/is-iana.validator.js';
import { HashWorkerPoolProvider } from './hash/hash-worker-pool.provider.js';
import { MyI18nModule } from '#/i18n/i18n.module.js';
import { RoleI18nService } from './i18n/role.i18n.js';
@Global() // 让这个模块在全局可用
@Module({
  providers: [
    DateFormatService,
    HashService,
    Bcp47LanguageValidator,
    IanaTimezoneValidator,
    HashWorkerPoolProvider,
    MyI18nModule,
    RoleI18nService,
  ], // 提供者
  exports: [
    DateFormatService,
    HashService,
    Bcp47LanguageValidator,
    IanaTimezoneValidator,
    RoleI18nService,
  ], // 导出提供者，以便其他模块可以使用
})
export class CommonModule {}
// 这个模块主要提供一些通用的服务和验证器，比如日期格式化、哈希处理等。
