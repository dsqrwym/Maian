import type { MongoAbility } from '@casl/ability';
import type { Action } from './actions.js';

export type ModelSubjects = {
  products: { user_id: string; status: string };
  categories: { user_id: string | undefined | null };
  users: { role: string; status: string };
  products_files: { user_id: string };
};

type SubjectName = keyof ModelSubjects;
export type ConsTomSubject = 'Admin' | 'Standard' | 'Enterprise';
export type AppAbility = MongoAbility<
  [Action, SubjectName | ConsTomSubject | ModelSubjects[SubjectName]]
>;
