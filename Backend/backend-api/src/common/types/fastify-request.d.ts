import { AuthenticatedUser, AuthTokenPayload } from '../../auth/auth.types';
declare module 'fastify' {
  interface FastifyRequest {
    user: AuthenticatedUser | AuthTokenPayload;
  }
}
