import type { UserPayload } from '@/auth/auth.types';
import type { AppAbility } from '@/casl/casl-types';
declare module 'fastify' {
  interface FastifyRequest {
    user: UserPayload;
    ability: AppAbility;
  }
}
