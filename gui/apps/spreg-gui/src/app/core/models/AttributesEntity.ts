import {PerunAttribute} from "./PerunAttribute";

export class AttributesEntity {
  constructor(attributes: any) {
    if (attributes) {
      if (attributes.hasOwnProperty('SERVICE')) {
        AttributesEntity.setToMap(attributes['SERVICE'], this.service);
      }
      if (attributes.hasOwnProperty('ORGANIZATION')) {
        AttributesEntity.setToMap(attributes['ORGANIZATION'], this.organization);
      }
      if (attributes.hasOwnProperty('PROTOCOL')) {
        AttributesEntity.setToMap(attributes['PROTOCOL'], this.protocol);
      }
      if (attributes.hasOwnProperty('ACCESS_CONTROL')) {
        AttributesEntity.setToMap(attributes['ACCESS_CONTROL'], this.accessControl);
      }
    }
  }

  private static setToMap(categoryAttrs: any, map: Map<string, PerunAttribute>): void {
    for (let [urn, attr] of Object.entries(categoryAttrs)) {
      map.set(urn, new PerunAttribute(attr));
    }
  }

  service: Map<string, PerunAttribute> = new Map<string, PerunAttribute>();
  organization: Map<string, PerunAttribute> = new Map<string, PerunAttribute>();
  protocol: Map<string, PerunAttribute> = new Map<string, PerunAttribute>();
  accessControl: Map<string, PerunAttribute> = new Map<string, PerunAttribute>();

  public serviceAttrs(): Map<string, PerunAttribute> {
    return this.service;
  }

  public organizationAttrs(): Map<string, PerunAttribute> {
    return this.organization;
  }

  public protocolAttrs(): Map<string, PerunAttribute> {
    return this.protocol;
  }

  public accessControlAttrs(): Map<string, PerunAttribute> {
    return this.accessControl;
  }
}
