import { UserRole } from '../../../prisma/generated';
import { AbilityBuilder } from '@casl/ability';
import { AppAbility } from '../casl-types';
import { Action } from '../actions';
import { UserPayload } from '../../auth/auth.types';
import { createPrismaAbility } from '@casl/prisma';

export class CaslAbilityFactory {
  constructor() {}
  createForUser(user?: Partial<UserPayload>) {
    const { can, build } = new AbilityBuilder<AppAbility>(createPrismaAbility);

    switch (user?.userRole) {
      case UserRole.ADMIN:
        can(Action.Access, 'Admin');
        can(Action.Create, 'Categories');
        can(Action.Update, 'Categories');
        can(Action.Read, 'Categories');
        break;
      case UserRole.SUPERADMIN:
        can(Action.Access, 'Admin');
        can(Action.Manage, 'Categories');
        break;
      case UserRole.RETAILER:
        can(Action.Access, 'Standard');
        can(Action.Read, 'Categories');
        break;
      case UserRole.WHOLESALER:
        can(Action.Access, 'Enterprise');
        can(Action.Manage, 'Categories', { user_id: user?.userId });
        break;
      case UserRole.DELIVERY:
        can(Action.Access, 'Enterprise');
        break;
      case UserRole.SUPPORT:
        can(Action.Access, 'Enterprise');
        break;
      case UserRole.WAREHOUSE: {
        can(Action.Access, 'Enterprise');
        can(Action.Manage, 'Categories', { user_id: user.wholesalerId });
        break;
      }
    }
    return build();
  }
}
