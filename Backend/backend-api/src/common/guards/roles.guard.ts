import {
  CanActivate,
  ExecutionContext,
  ForbiddenException,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { UserRole } from 'src/generated/prisma/client';
import { ROLES_ALLOWED_KEY } from './decorator/roles-allowed.decorator';
import { FastifyRequest } from 'fastify';
import { AUTH_ERROR } from '../../auth/auth.constants';

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
