export interface ICategoryResponseRelation {
  id: string;
  name: string;
  iva?: string | null;
  level?: number;
  user_id?: string | null;
  parent?: ICategoryResponseRelation | null;
  category_translations?:
    | {
        lang_code: string;
        name: string;
      }[]
    | null;
}

export interface ICategoryResponse {
  parent?: ICategoryResponseRelation | null;
  children?: ICategoryResponseRelation[] | null;
  children_count?: number;
  category_translations?:
    | {
        lang_code: string;
        name: string;
      }[]
    | null;
  user_id?: string | null;
  level?: number;
  iva?: string | null;
  id: bigint;
  name: string;
}
