// src/supabase/supabase.module.ts
import { Global, Module } from '@nestjs/common';
import { SupabaseService } from './supabase.service';

@Global() // 让这个模块在全局可用
@Module({
  providers: [SupabaseService],
  exports: [SupabaseService], // 让别的模块也能用
})
export class SupabaseModule {}