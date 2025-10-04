import { UserRole } from '../../../prisma/generated';
import { AbilityBuilder, createMongoAbility } from '@casl/ability';
import { AppAbility } from '../casl-types';
import { Action } from '../actions';
import { UserPayload } from '../../auth/auth.types';

export class CaslAbilityFactory {
  createForUser(user?: Partial<UserPayload>) {
    const { can, build } = new AbilityBuilder<AppAbility>(createMongoAbility);

    switch (user?.userRole) {
      case UserRole.ADMIN:
      case UserRole.SUPERADMIN:
        can(Action.Access, 'Admin');
        break;
      case UserRole.RETAILER:
        can(Action.Access, 'Standard');
        break;
      case UserRole.WHOLESALER:
      case UserRole.DELIVERY:
      case UserRole.SUPPORT:
      case UserRole.WAREHOUSE:
        can(Action.Access, 'Enterprise');
        break;
    }
    return build();
  }
}
