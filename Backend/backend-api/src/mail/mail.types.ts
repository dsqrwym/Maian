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
