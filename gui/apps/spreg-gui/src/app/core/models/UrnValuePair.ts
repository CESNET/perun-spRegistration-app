import {PerunAttributeDefinition} from "./PerunAttributeDefinition";
import {ApplicationItem} from "./ApplicationItem";

export class UrnValuePair {

  constructor(value: any, urn: string) {
    this.value = value;
    this.fullName = urn;
  }

  definition: PerunAttributeDefinition;
  value: any;
  oldValue: any;
  comment: string;
  fullName: string;
  input: ApplicationItem;

}
