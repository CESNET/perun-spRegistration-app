export class ProvidedService {
  constructor(item: any) {
    if (!item) {
      return;
    }

    this.name = new Map<string, string>();
    for (const k of Object.keys(item.name)) {
      this.name.set(k.toLowerCase(), item.name[k]);
    }

    this.description = new Map<string, string>();
    for (const k of Object.keys(item.description)) {
      this.description.set(k.toLowerCase(), item.description[k]);
    }

    this.id = item.facilityId;
    this.protocol = item.protocol;
    this.environment = item.environment;
  }

  id: number;
  name: Map<string, string>;
  description: Map<string, string>;
  environment: string;
  protocol: string;

}
