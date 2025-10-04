export interface BaseProfile {
  contact_name?: string; // 联系人姓名
  documents?: Document[]; // 资质/证件文件
  tax_number?: string; // 税号（VAT/NIF等）
}

export interface Document {
  type: 'license' | 'id_card' | 'other';
  file_url: string;
  verified?: boolean;
}
