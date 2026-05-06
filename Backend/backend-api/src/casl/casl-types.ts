import type { MongoAbility } from '@casl/ability';
import type { Action } from './actions.js';
import type { ProductStatus, UserStatus } from '#/generated/drizzle/enums.js';

export type ModelSubjects = {
  products: { user_id: string | undefined; status: ProductStatus };
  variant_products: { status: ProductStatus };
  categories: { user_id: string | undefined | null };
  users: { role: string; status: UserStatus };
  products_files: { user_id: string };
};

type SubjectName = keyof ModelSubjects;
export type ConsTomSubject = 'Admin' | 'Standard' | 'Enterprise';
export type AppAbility = MongoAbility<
  [Action, SubjectName | ConsTomSubject | ModelSubjects[SubjectName]]
>;
