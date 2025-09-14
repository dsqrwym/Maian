export function maskEmail(email: string): string {
  const [name, domain] = email.split('@');
  return name.length > 2 ? `${name.slice(0, 2)}***@${domain}` : `***@${domain}`;
}
