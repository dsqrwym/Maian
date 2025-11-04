import { PureAbility } from '@casl/ability';
import { Action } from './actions';
import { PrismaQuery, Subjects } from '@casl/prisma';
import { users } from '@prisma/client';

export type PrismaModels = {
  categories: { user_id: string | undefined };
  users: users;
};

type PrismaSubjects = Subjects<PrismaModels>;
export type ConsTomSubject = 'Admin' | 'Standard' | 'Enterprise';
export type AppAbility = PureAbility<
  [Action, PrismaSubjects | ConsTomSubject],
  PrismaQuery
>;
