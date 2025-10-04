import { UserPayload } from '../../auth/auth.types';
import { AppAbility } from '../../casl/casl-types';
declare module 'fastify' {
  interface FastifyRequest {
    user: UserPayload;
    ability: AppAbility;
  }
}
