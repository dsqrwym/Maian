import { Module } from '@nestjs/common';
import { AppController } from './app.controller.js';
import { AppService } from './app.service.js';

import { ConfigModule, ConfigService } from '@nestjs/config'; // 用于加载和管理应用程序的配置 比Node.js 自带的 process.env 更加安全和方便维护
import { LoggerModule } from 'nestjs-pino';

// 我自己的模块 :
//  公共模块
import { CommonModule } from './common/common.module.js'; // 全局的
// Redis 模块
import { CacheRedisModule } from './cache/cache.redis.module.js'; // 全局的
//  邮件模块
import { MailModule } from './mail/mail.module.js';
//  认证模块
import { AuthModule } from './auth/auth.module.js';
import { ResponseInterceptor } from './common/interceptor/response.interceptor.js';
import { Reflector, RouterModule } from '@nestjs/core';
import { HttpExceptionFilter } from './common/filters/http-exception.filter.js';
import { JwtExceptionFilter } from './common/filters/jwt-exception.filter.js';
import { DrizzleExceptionFilter } from './common/filters/drizzle-exception.filter.js';
import { ScheduleTaskModule } from './schedule-tasks/schedule-task.module.js';
import { MyI18nModule } from './i18n/i18n.module.js';
import { MyThrottlerModule } from './common/rate-limit/rate-limit.module.js';
import { REDIS_CACHE } from './cache/redis/cache.redis.token.js';
import { ENV } from './config/constants.config.js';
import { JwtModule } from '@nestjs/jwt';
import { LocationsModule } from './locations/locations.module.js';
import { CaslModule } from './casl/casl.module.js';
import { UserModule } from './user/user.module.js';
import { EnterpriseModule } from './enterprise/enterprise.module.js';
import { AdminModule } from './admin/admin.module.js';
import { CategoryModule } from './category/category.module.js';
import { ProductsModule } from './products/products.module.js';
import { FilesModule } from './files/files.module.js';
import { DrizzleModule } from './drizzle/drizzle.module.js';
import { CartsModule } from '#/carts/carts.module.js';
import { OrderModule } from '#/orders/order.module.js';

@Module({
  imports: [
    ConfigModule.forRoot(
      //forRoot() 代表初始化要使用的配置
      {
        isGlobal: true,
        envFilePath: ['.env'],
      }, // isGlobal: true 表示该模块在整个应用程序中都是可用的，而不仅仅是在导入它的模块中。这样可以避免在每个模块中都需要单独导入 ConfigModule。
    ), // 加载环境变量配置文件，默认加载 .env 文件中的变量
    // 默认内存缓存
    CacheRedisModule.register(REDIS_CACHE, ENV.REDIS_CACHE_URL), // Redis 缓存模块
    LoggerModule.forRootAsync({
      imports: [ConfigModule],
      inject: [ConfigService],
      useFactory: (config: ConfigService) => ({
        pinoHttp: {
          autoLogging: config.get<string>(ENV.NODE_ENV) !== 'production',
          transport:
            config.get<string>(ENV.NODE_ENV) !== 'production'
              ? {
                  targets: [
                    {
                      target: 'pino-pretty', // 开发环境使用 pino-pretty 格式化日志
                      options: {
                        destination: 1, // 1 表示 stdout
                        colorize: true, // 彩色输出
                        translateTime: 'SYS:standard', // 使用系统时间格式化
                      },
                    },
                  ],
                }
              : undefined,
          level:
            config.get<string>(ENV.NODE_ENV) === 'production'
              ? 'error'
              : 'debug', // 设置日志级别
          redact:
            config.get<string>(ENV.NODE_ENV) === 'production'
              ? [
                  'req.headers.authorization', // Bearer token
                  'req.headers.cookie',
                ]
              : undefined,
        },
        exclude:
          config.get<string>(ENV.NODE_ENV) === 'production'
            ? ['/(.*)']
            : undefined, // 排除所有路径，防止它为每个请求自动创建日志对象
      }),
    }),
    JwtModule.registerAsync({
      global: true,
      imports: [ConfigModule],
      inject: [ConfigService],
      useFactory: (config: ConfigService) => ({
        secret: config.get<string>(ENV.AUTH_JWT_SECRET),
        signOptions: {
          expiresIn: Number(config.get<number>(ENV.ACCESS_TOKEN_EXPIRES_IN)),
        },
      }),
    }),
    ScheduleTaskModule, // 开启定时任务
    MyI18nModule, // 语言翻译
    MyThrottlerModule, // 限流模块

    DrizzleModule, // 全局的模块
    FilesModule, // 全局的模块
    CommonModule, // 全局的模块

    MailModule, // 邮件模块
    AuthModule,
    LocationsModule,
    CaslModule,
    UserModule,
    EnterpriseModule,
    AdminModule,
    CategoryModule,
    ProductsModule,
    CartsModule,
    OrderModule,

    // 注册模块前的 prefix
    RouterModule.register([
      { path: 'files', module: FilesModule },
      { path: 'auth', module: AuthModule },
      { path: 'locations', module: LocationsModule },
      { path: 'user', module: UserModule },
      { path: 'enterprise', module: EnterpriseModule },
      { path: 'admin', module: AdminModule },
      { path: 'category', module: CategoryModule },
      { path: 'product', module: ProductsModule },
      { path: 'carts', module: CartsModule },
      { path: 'orders', module: OrderModule },
    ]),
  ],
  controllers: [AppController], // 控制器也是一个提供者，负责处理传入的请求和返回响应
  providers: [
    {
      provide: ResponseInterceptor,
      useFactory: (reflector: Reflector) => new ResponseInterceptor(reflector), // 通过工厂函数创建 ResponseInterceptor 实例
      inject: [Reflector], // 注入 Reflector 依赖项
    },

    AppService,
    HttpExceptionFilter, // 全局异常过滤器，处理 HTTP 异常
    JwtExceptionFilter, // 全局异常过滤器，处理 JWT 异常
    DrizzleExceptionFilter, // 全局异常过滤器，处理 Drizzle 数据库相关异常
  ], // 可以注入的服务
})
export class AppModule {}
