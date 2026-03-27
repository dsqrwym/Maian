import {
  BadRequestException,
  createParamDecorator,
  ExecutionContext,
} from '@nestjs/common';
import { FastifyRequest } from 'fastify';
import typia, { TypeGuardError } from 'typia';
import { IRequestBodyValidator } from '@nestia/core/src/options/IRequestBodyValidator';

/**
 * 自定义TypeBody装饰器，不使用@TypedBody 因为它不返回validator转换后的数据
 * * ### 说明
 * 1. **增强验证逻辑**：允许在 Typia 自动验证的基础上，添加 Typia 标签（Tags）无法实现的复杂业务验证。
 * 2. **数据清洗 (Sanitization)**：允许在验证通过后立即对数据进行处理（如 `trim` 字符串），弥补 Interface 模式下无法使用装饰器转换数据的不足。
 * * ### 使用
 * - **必须使用 Assert 模式**：传入的闭包应当基于 `typia.assert` 或 `typia.createAssert`。
 * - **泛型限制**：由于 TypeScript 泛型在运行时擦除，`typia` 无法直接对泛型 `T` 生成验证代码。因此请务必传入 `typia.createAssertEquals<T>()` 以利用 AOT 编译出的最高性能验证函数。
 */
export function TypedBody<T>(
  validator: IRequestBodyValidator.IAssert<T>,
): ParameterDecorator {
  return createParamDecorator(function TypedBody(
    _unknown: any,
    context: ExecutionContext,
  ) {
    const request: FastifyRequest = context.switchToHttp().getRequest();
    if (is_request_body_undefined(request))
      throw new BadRequestException('Request body is required.');
    else if (!isApplicationJson(request.headers['content-type']))
      throw new BadRequestException(
        `Request body type is not "application/json".`,
      );

    try {
      return validator.assert(request.body as T);
    } catch (exp) {
      if (typia.is<TypeGuardError>(exp)) {
        throw new BadRequestException({
          path: exp.path,
          reason: exp.message,
          expected: exp.expected,
          value: exp.value,
          message: MESSAGE,
        });
      }
      throw exp; // 其他未知错误继续抛
    }
  })();
}

/** @internal */
const isApplicationJson = (text?: string): boolean =>
  text !== undefined &&
  text
    .split(';')
    .map((str) => str.trim())
    .some((str) => str === 'application/json');

/** @internal */
export const is_request_body_undefined = (request: FastifyRequest): boolean =>
  request.headers['content-type'] === undefined &&
  (request.body === undefined ||
    (typeof request.body === 'object' &&
      request.body !== null &&
      Object.keys(request.body).length === 0));

/** @internal */
const MESSAGE = 'Request body data is not following the promised type.';
