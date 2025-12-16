import { ExecutionContext, Injectable } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';
import { CaslAbilityFactory } from '../../casl/casl-ability.factory/casl-ability.factory';
import { FastifyRequest } from 'fastify';

@Injectable()
export class JwtAuthGuard extends AuthGuard('my-jwt') {
  constructor(private readonly abilityFactory: CaslAbilityFactory) {
    super();
  }
  async canActivate(context: ExecutionContext) {
    const result = await super.canActivate(context);
    if (!result) return result;
    const request: FastifyRequest = context.switchToHttp().getRequest();
    request.ability = this.abilityFactory.createForUser(request.user);
    return true;
  }
}
