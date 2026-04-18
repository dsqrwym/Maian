export interface FindUserResponse {
  profile?: unknown;
  cif?: string | null;
  telephone?: string | null;
  last_name?: string | null;
  first_name?: string | null;
  email?: string;
  username?: string | null;
  user_id?: string | null;
  role?: string | null;
  status?: string | null;
  id: string;
}
