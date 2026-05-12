import type { IWholesalerProfile } from '#/enterprise/types/IWholesalerProfile.js';

export interface WholesalerProfileResponseDto {
  id: string;
  email: string;
  user_id: string | null;
  profile_image_file_id: bigint | null;
  first_name: string | null;
  last_name: string | null;
  username: string | null;
  telephone: string | null;
  tax_id: string | null;
  profile: IWholesalerProfile;
  store_directions: {
    street: string;
    zip_code: string;
    country: {
      name: string;
      name_local: string;
      iso_numeric: number;
    };
    province: {
      id: number;
      name: string;
      name_local: string;
    };
    city: {
      id: number;
      name: string;
      name_local: string;
    };
  };
}
