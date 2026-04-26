import type { BaseProfile } from '@/user/type/profile.type';
import type { SpanishCompanyType } from '@/auth/dto/register-wholesaler.dto';

export interface WholesalerProfileType extends BaseProfile {
  company_name: string;
  company_type: SpanishCompanyType;
}
