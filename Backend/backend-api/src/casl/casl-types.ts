import { PureAbility } from '@casl/ability';
import { Action } from './actions';
import { PrismaQuery, Subjects } from '@casl/prisma';

export type PrismaModels = {
  Categories: { user_id: string | undefined };
};

type PrismaSubjects = Subjects<PrismaModels>;
export type ConsTomSubject = 'Admin' | 'Standard' | 'Enterprise';
export type AppAbility = PureAbility<
  [Action, PrismaSubjects | ConsTomSubject],
  PrismaQuery
>;
