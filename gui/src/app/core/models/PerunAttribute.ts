import { PerunAttributeDefinition } from './PerunAttributeDefinition';
import { ApplicationItem } from './ApplicationItem';

export class PerunAttribute {
  constructor(item: any) {
    this.definition =
      'definition' in item
        ? new PerunAttributeDefinition(item.definition)
        : null;
    this.value = item.value;
    this.oldValue = item.oldValue;
    this.comment = item.comment;
    this.fullName = item.fullName;
    this.input = 'input' in item ? new ApplicationItem(item.input) : null;
  }

  definition: PerunAttributeDefinition;
  value: any;
  oldValue: any;
  comment: string;
  fullName: string;
  input: ApplicationItem;
}
