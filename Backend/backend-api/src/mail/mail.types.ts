type ResetPasswordJob = {
  user: { email: string; name: string; language?: string };
  code: string;
};

type RegisterEmailJob = {
  to: string;
  lang?: string;
  link: string;
  code: string;
};

export { ResetPasswordJob, RegisterEmailJob };
