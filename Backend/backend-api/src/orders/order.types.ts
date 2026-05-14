/**
 * 零售商快照
 */
export interface IRetailerSnapshot {
  id: string;
  userId: string;
  displayName?: string;
  companyName?: string;
  companyType?: string;
  contactName?: string;
  taxId: string;
  email: string;
  phone: string;
}

/**
 * 批发商快照
 */
export interface IWholesalerSnapshot {
  id: string;
  userId: string;
  displayName?: string;
  companyName: string;
  companyType: string;
  taxId: string;
  email: string;
  phone?: string;
}
