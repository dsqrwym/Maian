import type { BaseProfile } from '#/user/type/profile.type.js';
import type { SpanishCompanyType } from '#/auth/dto/register-wholesaler.dto.js';

export interface WholesalerProfileType extends BaseProfile {
  company_name: string;
  company_type: SpanishCompanyType;
}
