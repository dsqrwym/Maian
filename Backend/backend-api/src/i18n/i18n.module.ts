import { Module } from '@nestjs/common';
import {
  I18nModule,
  QueryResolver,
  AcceptLanguageResolver,
  I18nJsonLoader,
} from 'nestjs-i18n';
import { join } from 'path';

@Module({
  imports: [
    I18nModule.forRoot({
      fallbackLanguage: 'en',
      loader: I18nJsonLoader,
      loaderOptions: {
        path: join(process.cwd(), 'src', 'i18n', 'locales'), // i18n JSON 文件夹
        watch: false,
      },
      typesOutputPath: join(
        process.cwd(),
        'src',
        'i18n',
        'generated',
        'i18n.generated.ts',
      ), // i18n JSON 文件夹
      resolvers: [
        { use: QueryResolver, options: ['lang'] }, // ?lang=zh-CH
        AcceptLanguageResolver,
      ],
    }),
  ],
  exports: [I18nModule],
})
export class MyI18nModule {}
