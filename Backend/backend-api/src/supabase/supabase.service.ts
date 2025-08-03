import {
  HttpException,
  HttpStatus,
  Injectable,
  OnModuleInit,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import {
  AdminUserAttributes,
  createClient,
  SupabaseClient,
} from '@supabase/supabase-js';

function mapErrorCodeToHttpStatus(code?: string): number {
  switch (code) {
    case 'auth/email-already-exists':
      return HttpStatus.CONFLICT; // 409
    case 'auth/invalid-password':
    case 'auth/invalid-email':
      return HttpStatus.BAD_REQUEST; // 400
    case 'auth/user-not-found':
      return HttpStatus.NOT_FOUND; // 404
    default:
      return HttpStatus.INTERNAL_SERVER_ERROR; // 500
  }
}

@Injectable() //表明这是可以注入的服务
export class SupabaseService implements OnModuleInit {
  private supabaseClient: SupabaseClient; //客户端
  constructor(private config: ConfigService) {}

  onModuleInit() {
    // 读取环境变量中的 Supabase URL 和 Service Role Key
    const supabaseUrl = this.config.get<string>('SUPABASE_URL')!; // ! 表示非空，避免 TypeScript 报错
    const supabaseKey = this.config.get<string>('SUPABASE_SERVICE_ROLE_KEY')!;
    // 初始化创建 Supabase 客户端
    this.supabaseClient = createClient(supabaseUrl, supabaseKey, {
      auth: {
        persistSession: false, // 不持久化会话(没有必要)
        autoRefreshToken: false, // 不自动刷新令牌(我自己控制用户token的刷新)
        detectSessionInUrl: false, // 不检测 URL 中的会话， 服务器根本没有URL OAuth 自动重定向
        debug: true, // 调试模式
      },
    });
  }

  getClient(): SupabaseClient {
    return this.supabaseClient; //返回客户端
  }

  /**
   * 用Supabase 的 Admin API 进行数据库操作
   * 使用其 Auth 系统 进行用户管理
   * 只进行最基本的，实际上用户管理是通过我自己表格来进行的
   */

  async createUser(email: string, password_hash: string) {
    const { data, error } = await this.supabaseClient.auth.admin.createUser({
      email: email,
      password_hash: password_hash,
    });
    if (error) {
      throw new HttpException(
        `${error.message} \n Supabase error code: ${error.code}`,
        error.status || mapErrorCodeToHttpStatus(error.code),
      ); // 如果发生错误，抛出 HTTP 异常
    }
    return data; // 返回创建的用户数据;
  }

  async getUserById(id: string) {
    const { data, error } =
      await this.supabaseClient.auth.admin.getUserById(id);
    if (error) {
      throw new HttpException(
        `${error.message} \n Supabase error code: ${error.code}`,
        error.status || mapErrorCodeToHttpStatus(error.code),
      ); // 如果发生错误，抛出 HTTP 异常
    }
    return data; // 返回用户数据
  }

  async updateUserbyId(id: string, attributes: AdminUserAttributes) {
    const { data, error } = await this.supabaseClient.auth.admin.updateUserById(
      id,
      attributes,
    );
    if (error) {
      throw new HttpException(
        `${error.message} \n Supabase error code: ${error.code}`,
        error.status || mapErrorCodeToHttpStatus(error.code),
      ); // 如果发生错误，抛出 HTTP 异常
    }
    return data;
  }
  async deleteUser(id: string) {
    const { data, error } = await this.supabaseClient.auth.admin.deleteUser(id);
    if (error) {
      throw new HttpException(
        `${error.message} \n Supabase error code: ${error.code}`,
        error.status || mapErrorCodeToHttpStatus(error.code),
      ); // 如果发生错误，抛出 HTTP 异常
    }
    return data;
  }
}
