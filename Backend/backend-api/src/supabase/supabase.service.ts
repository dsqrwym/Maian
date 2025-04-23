import { Injectable, OnModuleInit } from '@nestjs/common';
import { createClient, SupabaseClient } from '@supabase/supabase-js';


@Injectable() //表明这是可以注入的服务
export class SupabaseService implements OnModuleInit {
    private supabaseClient: SupabaseClient; //客户端

    onModuleInit() {
        // 初始化创建 Supabase 客户端
        this.supabaseClient = createClient(
            process.env.SUPABASE_URL!, // ! 表示非空，避免 TypeScript 报错
            process.env.SUPABASE_SERVICE_ROLE_KEY!,
            {
                auth: {
                    persistSession: false, // 不持久化会话(没有必要)
                    autoRefreshToken: false // 不自动刷新令牌(我自己控制用户token的刷新)
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

    async createUser(){
        this.supabaseClient.auth.admin.createUser({

        });
    }
}
