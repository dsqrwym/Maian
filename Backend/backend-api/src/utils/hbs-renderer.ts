import { join } from 'path';
import { readFileSync } from 'node:fs';
import Handlebars from 'handlebars';

Handlebars.registerHelper('eq', (a: any, b: any) => a === b);

export function renderTemplate(
  templateName: string,
  context?: Record<string, any>,
): string {
  const templatePath = join(
    process.cwd(),
    'src',
    'mail',
    'templates',
    `${templateName}.hbs`,
  );
  const source = readFileSync(templatePath, 'utf8');
  const template = Handlebars.compile(source);
  return template(context);
}
