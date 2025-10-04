import { SetMetadata } from '@nestjs/common';
import { UserRole } from '../../../../prisma/generated';

export const ROLES_ALLOWED_KEY = 'ROLES_ALLOWED_KEY';

export const RolesAllowed = (...roles: UserRole[]) =>
  SetMetadata(ROLES_ALLOWED_KEY, roles);
