import type { SpanishCompanyType } from '#/auth/dto/register-wholesaler.dto.js';
import type {
  TagsCompanyName,
  TagsRetailerContactName,
  TagsRetailerDisplayName,
} from '#/utils/typia/validators/user.validator.js';

export interface IRetailerProfile {
  company_name?: TagsCompanyName | null;
  display_name?: TagsRetailerDisplayName | null;
  company_type?: SpanishCompanyType | null;
  contact_name?: TagsRetailerContactName | null;
}
