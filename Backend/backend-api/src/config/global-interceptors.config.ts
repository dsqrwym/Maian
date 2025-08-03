import { INestApplication } from '@nestjs/common';
import { ResponseInterceptor } from '../common/interceptor/response.interceptor';

export function useGlobalInterceptors(app: INestApplication) {
  app.useGlobalInterceptors(app.get(ResponseInterceptor)); // 全局拦截器，统一响应格式
}
