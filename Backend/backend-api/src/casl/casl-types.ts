import { MongoAbility } from '@casl/ability';
import { Action } from './actions';

export type Subject = 'Admin' | 'Standard' | 'Enterprise';
export type AppAbility = MongoAbility<[Action, Subject]>;
