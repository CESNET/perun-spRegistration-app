export class ProvidedService {
  constructor(item: any) {
    if (!item) {
      return;
    }

    this.name = new Map<string, string>();
    if (item.hasOwnProperty('description') && item.name !== null && item.name !== undefined) {
      for (const k of Object.keys(item.name)) {
        this.name.set(k.toLowerCase(), item.name[k]);
      }
    }

    this.description = new Map<string, string>();
    if (item.hasOwnProperty('description') && item.description !== null && item.description !== undefined) {
      for (const k of Object.keys(item.description)) {
        this.description.set(k.toLowerCase(), item.description[k]);
      }
    }

    this.id = item.id;
    this.facilityId = item.facilityId;
    this.identifier = item.identifier;
    this.protocol = item.protocol;
    this.environment = item.environment;
  }

  id: number;
  facilityId: number;
  name: Map<string, string>;
  description: Map<string, string>;
  identifier: string;
  environment: string;
  protocol: string;

}
