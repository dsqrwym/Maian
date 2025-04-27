import { Global, Module } from '@nestjs/common';
import { DateFormatService } from './services/date-format.service';
import { HashService } from './services/hash.service';
import { Bcp47LanguageValidator } from './validators/is-bcp47-language.validator';
import { IanaTimezoneValidator } from './validators/is-iana.validator';

@Global() // 让这个模块在全局可用
@Module({
  providers: [DateFormatService, HashService, Bcp47LanguageValidator, IanaTimezoneValidator], // 提供者
  exports: [DateFormatService, HashService], // 导出提供者，以便其他模块可以使用
})
export class CommonModule {}
// 这个模块主要提供一些通用的服务和验证器，比如日期格式化、哈希处理等。