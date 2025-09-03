import { NestFastifyApplication } from '@nestjs/platform-fastify';
import fastifyCookie from '@fastify/cookie';
import { ConfigService } from '@nestjs/config';
import { ENV } from './constants';

export async function useCookie(app: NestFastifyApplication) {
  // Prefer ConfigService (.env), fallback to process.env
  const configService = app.get(ConfigService);
  const secret =
    configService.get<string>(ENV.COOKIE_SECRET) || process.env.COOKIE_SECRET;

  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  // @ts-expect-error
  await app.register(fastifyCookie, {
    secret: secret,
  });
}
