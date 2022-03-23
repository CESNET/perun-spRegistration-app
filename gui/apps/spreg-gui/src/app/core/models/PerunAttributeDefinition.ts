export class PerunAttributeDefinition {

  constructor(item: any) {
    this.friendlyName = item.friendlyName;
    this.namespace = item.namespace;
    this.description = item.description;
    this.type = item.type;
    this.displayName = item.displayName;
    this.writable = item.writable;
    this.entity = item.entity;
    this.beanName = item.beanName;
  }

  friendlyName: string;
  namespace: string;
  description: string;
  type: string;
  displayName: string;
  writable: boolean;
  entity: string;
  beanName: string;
}
