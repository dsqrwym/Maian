import type { UserPayload } from '#/auth/auth.types.js';
import type { AppAbility } from '#/casl/casl-types.js';
declare module 'fastify' {
  interface FastifyRequest {
    user: UserPayload;
    ability: AppAbility;
  }
}
