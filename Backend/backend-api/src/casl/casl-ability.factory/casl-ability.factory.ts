import { AbilityBuilder, createMongoAbility } from '@casl/ability';
import type { AppAbility } from '../casl-types.js';
import { Action } from '../actions.js';
import type { UserPayload } from '#/auth/auth.types.js';
import {
  ProductStatus,
  UserRole,
  UserStatus,
} from '#/generated/drizzle/enums.js';

export class CaslAbilityFactory {
  constructor() {}
  createForUser(user: Partial<UserPayload>) {
    const { can, cannot, build } = new AbilityBuilder<AppAbility>(
      createMongoAbility,
    );

    switch (user.userRole) {
      case UserRole.ADMIN:
        can(Action.Access, 'Admin');
        can(Action.Manage, 'users');
        cannot(Action.Manage, 'users', { role: UserRole.ADMIN });
        cannot(Action.Manage, 'users', { role: UserRole.SUPERADMIN });
        can(Action.Create, 'categories');
        can(Action.Update, 'categories');
        can(Action.Read, 'categories');
        can(Action.Manage, 'products');
        can(Action.Manage, 'products_files');
        break;
      case UserRole.SUPERADMIN:
        can(Action.Access, 'Admin');
        can(Action.Manage, 'users');
        can(Action.Manage, 'categories');
        can(Action.Manage, 'products');
        can(Action.Manage, 'products_files');
        break;
      case UserRole.RETAILER:
        can(Action.Access, 'Standard');
        can(Action.Read, 'users', { role: UserRole.WHOLESALER });
        cannot(Action.Read, 'users', { status: UserStatus.INACTIVE });
        cannot(Action.Read, 'users', { status: UserStatus.BANNED });
        cannot(Action.Read, 'users', {
          status: UserStatus.PENDING_VERIFICATION,
        });
        cannot(Action.Read, 'users', { status: UserStatus.PENDING_REVIEW });
        can(Action.Read, 'categories');
        can(Action.Read, 'products', { status: ProductStatus.ACTIVE });
        // 这个只是用于将 零售商只能看 ACTIVE variant 也可以转为 where conditions
        can(Action.Read, 'variant_products', { status: ProductStatus.ACTIVE });
        can(Action.Read, 'products_files');
        break;
      case UserRole.WHOLESALER:
        can(Action.Access, 'Enterprise');
        can(Action.Manage, 'categories', { user_id: user.userId });
        can(Action.Manage, 'products', { user_id: user.userId });
        can(Action.Manage, 'products_files', { user_id: user.userId });
        can(Action.Update, 'users', { id: user.userId });
        can(Action.Read, 'users', { id: user.userId });
        break;
      case UserRole.DELIVERY:
        can(Action.Access, 'Enterprise');
        can(Action.Read, 'products', { user_id: user.wholesalerId });
        can(Action.Read, 'products_files', { user_id: user.wholesalerId });
        break;
      case UserRole.SUPPORT:
        can(Action.Access, 'Enterprise');
        can(Action.Read, 'products', { user_id: user.wholesalerId });
        can(Action.Read, 'products_files', { user_id: user.wholesalerId });
        break;
      case UserRole.WAREHOUSE: {
        can(Action.Access, 'Enterprise');
        can(Action.Manage, 'categories', { user_id: user.wholesalerId });
        can(Action.Manage, 'products', { user_id: user.wholesalerId });
        can(Action.Manage, 'products_files', { user_id: user.wholesalerId });
        break;
      }
    }
    return build();
  }
}
