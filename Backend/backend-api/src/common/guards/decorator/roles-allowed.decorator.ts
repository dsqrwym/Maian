import { SetMetadata } from '@nestjs/common';
import { UserRole } from '../../../generated/drizzle/enums';

export const ROLES_ALLOWED_KEY = 'ROLES_ALLOWED_KEY';

/**
 * Decorator to specify allowed roles for a controller or route
 * 只用于控制用户访问接口的权限，主要用于在GET这些使用CASL进行查询限制而不是访问限制的入口
 */
export const RolesAllowed = (...roles: UserRole[]) =>
  SetMetadata(ROLES_ALLOWED_KEY, roles);
