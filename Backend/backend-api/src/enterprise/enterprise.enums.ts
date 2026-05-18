import { UserRole } from '#/generated/drizzle/enums.js';

export enum EmployeeSortByFields {
  USER_ID = 'user_id',
  FirstName = 'first_name',
  LastName = 'last_name',
  Email = 'email',
  Username = 'username',
  Telephone = 'telephone',
  TaxId = 'tax_id',
}

export enum EmployeeRole {
  SUPPORT = 'SUPPORT',
  DELIVERY = 'DELIVERY',
  WAREHOUSE = 'WAREHOUSE',
}
