import { ReqUser } from '../../auth/auth.types';

declare module 'fastify' {
  interface FastifyRequest {
    user: ReqUser;
  }
}
