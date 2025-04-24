import { Injectable, OnModuleInit } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { createClient, SupabaseClient } from '@supabase/supabase-js';


@Injectable() //表明这是可以注入的服务
export class SupabaseService implements OnModuleInit {
    private supabaseClient: SupabaseClient; //客户端
    constructor(private config: ConfigService) { }

    onModuleInit() {
        // 读取环境变量中的 Supabase URL 和 Service Role Key
        const supabaseUrl = this.config.get<string>('SUPABASE_URL')!; // ! 表示非空，避免 TypeScript 报错
        const supabaseKey = this.config.get<string>('SUPABASE_SERVICE_ROLE_KEY')!;
        // 初始化创建 Supabase 客户端
        this.supabaseClient = createClient(
            supabaseUrl,
            supabaseKey,
            {
                auth: {
                    persistSession: false, // 不持久化会话(没有必要)
                    autoRefreshToken: false, // 不自动刷新令牌(我自己控制用户token的刷新)
                    detectSessionInUrl: false, // 不检测 URL 中的会话， 服务器根本没有URL OAuth 自动重定向
                    debug: true // 调试模式
                }
            }
        );
    }

    getClient(): SupabaseClient {
        return this.supabaseClient; //返回客户端
    }

    /**
     * 用Supabase 的 Admin API 进行数据库操作
     * 使用其 Auth 系统 进行用户管理
     */

    async createUser() {
        this.supabaseClient.auth.admin.createUser({

        });
    }
}
