import {
  CanActivate,
  ExecutionContext,
  ForbiddenException,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { UserRole } from '#/generated/drizzle/enums.js';
import { ROLES_ALLOWED_KEY } from './decorator/roles-allowed.decorator.js';
import { FastifyRequest } from 'fastify';
import { AUTH_ERROR } from '#/auth/auth.constants.js';

/**
 * Role Guard
 * 只用于控制用户访问接口的权限，主要用于在GET这些使用CASL进行查询限制而不是访问限制的入口
 */
@Injectable()
export class RolesGuard implements CanActivate {
  constructor(private readonly reflector: Reflector) {}
  canActivate(context: ExecutionContext): boolean {
    const allowedRoles = this.reflector.getAllAndOverride<UserRole[]>(
      ROLES_ALLOWED_KEY,
      [context.getHandler(), context.getClass()],
    );
    if (!allowedRoles) return true;

    const request = context.switchToHttp().getRequest<FastifyRequest>();
    const user = request.user;

    if (!user) throw new UnauthorizedException(AUTH_ERROR.NO_AUTH_PAYLOAD);
    if (allowedRoles.includes(user.userRole)) return true;

    throw new ForbiddenException(AUTH_ERROR.ACCESS_DENIED);
  }
}
