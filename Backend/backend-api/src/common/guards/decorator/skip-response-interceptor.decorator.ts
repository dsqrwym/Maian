import { SetMetadata } from '@nestjs/common';

export const SKIP_RESPONSE_INTERCEPTOR = 'SKIP_RESPONSE_INTERCEPTOR';

/**
 * 标记该路由应跳过 ResponseInterceptor 的数据封装和序列化逻辑。
 */
export const SkipResponseInterceptor = () =>
  SetMetadata(SKIP_RESPONSE_INTERCEPTOR, true);
