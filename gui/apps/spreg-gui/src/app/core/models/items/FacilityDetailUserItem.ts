import {User} from "../User";

export class FacilityDetailUserItem {

  constructor(user: User) {
    this.id = user.id;
    this.name = user.name;
    this.email = user.email;
  }

  id: number;
  name: string;
  email: string;
}
