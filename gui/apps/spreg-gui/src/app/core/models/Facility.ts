import {User} from './User';
import {AttributesEntity} from "./AttributesEntity";
import {FacilityDetailUserItem} from "./items/FacilityDetailUserItem";

export class Facility extends AttributesEntity {
  constructor(item: any) {
    if (!item) {
      return;
    }
    super(item.attributes);
    this.id = item.id;
    this.perunName = item.perunName;
    this.perunDescription = item.perunDescription;
    this.name = new Map<string, string>();
    if (item.hasOwnProperty('name') && item.name) {
      for (const k of Object.keys(item.name)) {
        this.name.set(k.toLowerCase(), item.name[k]);
      }
    }

    this.description = new Map<string, string>();
    if (item.hasOwnProperty('description') && item.description) {
      for (const k of Object.keys(item.description)) {
        this.description.set(k.toLowerCase(), item.description[k]);
      }
    }

    this.activeRequestId = item.activeRequestId;
    this.environment = item.environment;
    this.protocolUsed = item.protocol;

    this.managers = [];
    if (item.managers) {
      item.managers.forEach(user => {
        this.managers.push(new User(user));
      });
    }
    this.deleted = item.deleted;
  }

  id: number;
  perunName: string;
  perunDescription: string;
  name: Map<string, string>;
  description: Map<string, string>;
  environment: string;
  protocolUsed: string;
  activeRequestId: number;
  managers: User[];
  deleted: boolean;

}
