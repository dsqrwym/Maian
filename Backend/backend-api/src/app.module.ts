import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';

import { ConfigModule } from '@nestjs/config'; // 用于加载和管理应用程序的配置 比Node.js 自带的 process.env 更加安全和方便维护

// 我自己的模块 : 
//  邮件模块
import { MailService } from './mail/mail.service';
import { DateFormatService } from './common/services/date-format.service';



@Module({
  imports: [
    ConfigModule.forRoot( //forRoot() 代表初始化要使用的配置
      { isGlobal: true, } // isGlobal: true 表示该模块在整个应用程序中都是可用的，而不仅仅是在导入它的模块中。这样可以避免在每个模块中都需要单独导入 ConfigModule。
    ), // 加载环境变量配置文件，默认加载 .env 文件中的变量
  ],
  controllers: [AppController],
  providers: [AppService, MailService, DateFormatService], // 所有可以注入的服务
  exports: [MailService], // 导出模块 以便在其他模块中使用
})
export class AppModule { }
