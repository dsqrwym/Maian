import { Injectable } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';

@Injectable()
export class LocalAuthGuard extends AuthGuard('custom-local') {}

@Injectable()
export class JwtAuthGuard extends AuthGuard('my-jwt') {}
