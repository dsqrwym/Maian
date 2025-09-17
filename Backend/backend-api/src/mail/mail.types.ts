type VerificationEmailJob = {
  to: string;
  lang?: string;
  link: string;
  date: Date;
  timeZone?: string;
};

type ResetPasswordJob = {
  user: { email: string; name: string; language?: string };
  code: string;
};

export { VerificationEmailJob, ResetPasswordJob };
