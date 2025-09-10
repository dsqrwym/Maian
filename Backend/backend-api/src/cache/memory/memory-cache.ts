import { CACHE_MANAGER, CacheModule } from '@nestjs/cache-manager';
import { Global, Module } from '@nestjs/common';

export const MEMORY_CACHE = 'MEMORY_CACHE';

@Global()
@Module({
  imports: [
    CacheModule.register({
      isGlobal: false,
    }),
  ],
  providers: [
    {
      provide: MEMORY_CACHE,
      useExisting: CACHE_MANAGER,
    },
  ],
  exports: [MEMORY_CACHE],
})
export class MemoryCacheModule {}
