import {
  type PrismaModel,
  type PrismaQueryFactory,
  type PrismaTypes,
  createAbilityFactory,
} from '@casl/prisma/runtime';
import { Prisma } from 'prisma/generated/client';

export { accessibleBy, ParsingQueryError } from '@casl/prisma/runtime';
export type { Model, Subjects } from '@casl/prisma/runtime';
export type WhereInput<TModelName extends Prisma.ModelName> =
  PrismaTypes<Prisma.TypeMap>['WhereInput'][TModelName];
export type PrismaQuery<T extends PrismaModel = PrismaModel> =
  PrismaQueryFactory<Prisma.TypeMap, T>;
export const createPrismaAbility = createAbilityFactory<
  Prisma.ModelName,
  PrismaQuery
>();
