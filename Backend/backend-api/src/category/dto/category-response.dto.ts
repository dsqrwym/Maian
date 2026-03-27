interface ICategoryTranslation {
  name: string;
  lang_code: string;
}

interface ICategoryChild {
  id: string | number;
  name: string;
  iva?: string | number | null;
}

interface ICategoryParent {
  id: string | number;
  name: string;
  iva?: string | number | null;
  parent?: ICategoryParent | null;
}

export interface ICategoryResponse {
  id: bigint;
  name: string;
  iva?: number;
  category_translations?: ICategoryTranslation[];
  parent?: ICategoryParent | null;
  children?: ICategoryChild[];
  children_count?: bigint;
}
