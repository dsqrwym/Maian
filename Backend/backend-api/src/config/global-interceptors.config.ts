import { ResponseInterceptor } from '#/common/interceptor/response.interceptor.js';
import type { NestFastifyApplication } from '@nestjs/platform-fastify';
import type { PaginatedData } from '#/common/types-interfaces/response.interface.js';
import type { FastifyReply, RawServerDefault } from 'fastify';
import { isJson } from '#/utils/is.utils.js';

export function useGlobalInterceptors(
  app: NestFastifyApplication<RawServerDefault>,
) {
  app.useGlobalInterceptors(app.get(ResponseInterceptor)); // 全局拦截器，统一响应格式

  app
    .getHttpAdapter()
    .getInstance()
    .addHook('onSend', async (_, res, payload) => {
      const metadata = (res as unknown as FastifyReply)._nestMetadata;

      // 如果没有元数据或明确跳过，直接返回
      if (!metadata || metadata.skip) return payload;
      if (isJson(payload)) {
        return `{"statusCode":${res.statusCode},"message":${
          metadata.message ? JSON.stringify(metadata.message) : 'success'
        },"data":${payload}}`;
      }

      //payload 是对象（非 Typia 路径，或者是还没被序列化的数据）我们需要手动执行 JSON.stringify，因为 onSend 不允许返回 Object
      return JSON.stringify({
        statusCode: res.statusCode,
        message: metadata.message || 'success',
        data: serializeData(payload),
      });
    });
}

function serializeData<T>(data: unknown): T | PaginatedData {
  if (isPaginatedDate(data)) {
    return data;
  }
  return data as T;
}

// 类型守卫函数
function isPaginatedDate(data: unknown): data is PaginatedData {
  return (
    !!data &&
    typeof data === 'object' &&
    'items' in data &&
    'pagination' in data &&
    Array.isArray(data.items)
  );
}
