export interface BaseEmailJob {
  to: string;
  lang?: string;
}

export interface BaseEmailJobWithLink extends BaseEmailJob {
  link: string;
}

export interface BaseEmailJovWithTemporaryPassword extends BaseEmailJob {
  temporaryPassword: string;
}

export interface ResetPasswordJob extends BaseEmailJob {
  name: string;
  code: string;
}

export interface RegisterEmailJob extends BaseEmailJobWithLink {
  code: string;
}

export interface VerifyEmployeeEmailJob extends BaseEmailJobWithLink {
  companyName: string;
  position: string;
}

export interface ActiveEmployeeWithPasswordEmailJob extends BaseEmailJovWithTemporaryPassword {
  employeeName: string;
  companyName: string;
}

export interface ActiveAdminWithPasswordEmailJob extends BaseEmailJovWithTemporaryPassword {
  adminName: string;
}

export enum OrderPdfEmailType {
  NEW_ORDER = 'NEW_ORDER',
  ORDER_ACCEPTED = 'ORDER_ACCEPTED',
  ORDER_REJECTED = 'ORDER_REJECTED',
  ORDER_CANCELLED = 'ORDER_CANCELLED',
}

export interface OrderPdfNotificationEmailJob extends BaseEmailJob {
  type: OrderPdfEmailType;
  orderNumber: string;
  fileId: string;
  recipientName?: string | null;
  counterpartyName?: string | null;
  actionReason?: string | null;
}
