import { ThrottlerGuard } from '@nestjs/throttler';
import { Injectable } from '@nestjs/common';
import { FastifyRequest } from 'fastify';
import { isObject } from '@/utils/is.utils';

@Injectable()
export class CustomThrottlerGuard extends ThrottlerGuard {
  protected getTracker(req: FastifyRequest): Promise<string> {
    const authTokenPayload = req.user;
    if (authTokenPayload) {
      return Promise.resolve(`session:${authTokenPayload.sessionId}`);
    }

    if (isObject(req.body)) {
      const email: unknown = req.body?.email;
      if (email && typeof email === 'string') {
        return Promise.resolve(`email:${email}`);
      }
    }

    const ip = req.ip;
    const ua = req.headers['user-agent'] || 'unknown-user-agent';
    const ra = req.socket.remoteAddress || 'unknown-remote-address';
    return Promise.resolve(`ip-ua-ra:${ip}-${ua}-${ra}`);
  }
}
