import { Global, Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';

import { ConfigModule } from '@nestjs/config'; // 用于加载和管理应用程序的配置 比Node.js 自带的 process.env 更加安全和方便维护

// 我自己的模块 : 
//  公共模块
import { CommonModule } from './common/common.module'; // 全局的
//  supabase 模块
import { SupabaseModule } from './supabase/supabase.module'; // 全局的
//  Prisma 模块
import { PrismaModule } from './prisma/prisma.module'; // 全局的
//  邮件模块
import { MailModule } from './mail/mail.module';
//  认证模块
import { AuthModule } from './auth/auth.module';
import { CacheModule } from '@nestjs/cache-manager';
import { ResponseInterceptor } from './common/interceptor/response.interceptor';
import { Reflector } from '@nestjs/core';


@Module({
  imports: [
    ConfigModule.forRoot( //forRoot() 代表初始化要使用的配置
      { isGlobal: true, } // isGlobal: true 表示该模块在整个应用程序中都是可用的，而不仅仅是在导入它的模块中。这样可以避免在每个模块中都需要单独导入 ConfigModule。
    ), // 加载环境变量配置文件，默认加载 .env 文件中的变量
    CacheModule.register(
      { isGlobal: true, } // 全局缓存模块
    ),
    CommonModule, // 全局的模块
    PrismaModule, // 全局的模块
    SupabaseModule, // 全局的模块
    MailModule, // 邮件模块
    AuthModule, // 认证模块
  ],
  controllers: [AppController], // 控制器也是一个提供者，负责处理传入的请求和返回响应
  providers: [AppService,
    {
      provide: ResponseInterceptor,
      useFactory: (reflector: Reflector) => new ResponseInterceptor(reflector), // 通过工厂函数创建 ResponseInterceptor 实例
      inject: [Reflector], // 注入 Reflector 依赖项
    }
  ], // 可以注入的服务
})
export class AppModule { }
