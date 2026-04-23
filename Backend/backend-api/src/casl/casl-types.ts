import { PureAbility } from '@casl/ability';
import { Action } from './actions';
import { PrismaQuery, Subjects } from '@casl/prisma';
import { users } from 'src/generated/prisma/client';

export type PrismaModels = {
  products: { user_id: string };
  categories: { user_id: string | undefined | null };
  users: users;
  products_files: { user_id: string };
};

type PrismaSubjects = Subjects<PrismaModels>;
export type ConsTomSubject = 'Admin' | 'Standard' | 'Enterprise';
export type AppAbility = PureAbility<
  [Action, PrismaSubjects | ConsTomSubject],
  PrismaQuery
>;
