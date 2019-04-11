export interface User {
  titleBefore: string;
  firstName: string;
  middleName: string;
  lastName: string;
  titleAfter: string;
  fullName: string;
  email: string;
  facilitiesWhereUserIsAdmin: number[];
  isAdmin: boolean;
}
