import { BaseProfile } from './profile.type';
import { SpanishCompanyType } from '../../auth/dto/register-wholesaler.dto';

export interface WholesalerProfileType extends BaseProfile {
  company_name: string;
  company_type: SpanishCompanyType;
}
