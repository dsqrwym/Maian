import { UserRole, UserStatus } from 'src/generated/prisma/client';
import { AbilityBuilder } from '@casl/ability';
import { AppAbility } from '../casl-types';
import { Action } from '../actions';
import { UserPayload } from '../../auth/auth.types';
import { createPrismaAbility } from '@casl/prisma';

export class CaslAbilityFactory {
  constructor() {}
  createForUser(user: Partial<UserPayload>) {
    const { can, build } = new AbilityBuilder<AppAbility>(createPrismaAbility);

    switch (user.userRole) {
      case UserRole.ADMIN:
        can(Action.Access, 'Admin');
        can(Action.Manage, 'users', {
          role: { notIn: [UserRole.ADMIN, UserRole.SUPERADMIN] },
        });
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
        can(Action.Read, 'users', {
          role: UserRole.WHOLESALER,
          status: {
            notIn: [
              UserStatus.INACTIVE,
              UserStatus.BANNED,
              UserStatus.PENDING_VERIFICATION,
              UserStatus.PENDING_REVIEW,
            ],
          },
        });
        can(Action.Read, 'categories');
        can(Action.Read, 'products', { status: 'ACTIVE' });
        can(Action.Read, 'products_files');
        break;
      case UserRole.WHOLESALER:
        can(Action.Access, 'Enterprise');
        can(Action.Manage, 'categories', { user_id: user.userId });
        can(Action.Manage, 'products', { user_id: user.userId });
        can(Action.Manage, 'products_files', { user_id: user.userId });
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
