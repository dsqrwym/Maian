import {
  CanActivate,
  ExecutionContext,
  ForbiddenException,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { CaslAbilityFactory } from '../../casl/casl-ability.factory/casl-ability.factory';
import { Observable } from 'rxjs';
import { UserRole } from '../../../prisma/generated';
import { ROLES_ALLOWED_KEY } from './decorator/roles-allowed.decorator';
import { FastifyRequest } from 'fastify';
import { AUTH_ERROR } from '../../auth/auth.constants';

@Injectable()
export class RolesGuard implements CanActivate {
  constructor(
    private readonly reflector: Reflector,
    private readonly caslAbilityFactory: CaslAbilityFactory,
  ) {}
  canActivate(
    context: ExecutionContext,
  ): boolean | Promise<boolean> | Observable<boolean> {
    const allowedRoles = this.reflector.getAllAndOverride<UserRole[]>(
      ROLES_ALLOWED_KEY,
      [context.getHandler(), context.getClass()],
    );
    if (!allowedRoles) return false;

    const request = context.switchToHttp().getRequest<FastifyRequest>();
    const user = request.user;
    if (!user) throw new UnauthorizedException(AUTH_ERROR.NO_AUTH_PAYLOAD);

    request.ability = this.caslAbilityFactory.createForUser(user);

    if (allowedRoles.includes(user.userRole)) return true;

    throw new ForbiddenException(AUTH_ERROR.ACCESS_DENIED);
  }
}
