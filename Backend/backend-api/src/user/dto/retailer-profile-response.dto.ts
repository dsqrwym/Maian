import type { IRetailerProfile } from '#/user/type/retailer-profile.type.js';

export interface RetailerProfileResponseDto {
  id: string;
  email: string;
  user_id: string | null;
  profile_image_file_id: bigint | null;
  first_name: string | null;
  last_name: string | null;
  username: string | null;
  telephone: string | null;
  tax_id: string | null;
  profile: IRetailerProfile | null;
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
  } | null;
}
