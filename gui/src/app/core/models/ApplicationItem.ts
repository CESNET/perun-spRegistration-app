export class ApplicationItem {

  constructor(item: any) {
    if (!item) {
      return
    }
    this.name = item.name;

    this.description = new Map<string, string>();
    if (item.hasOwnProperty('description') && item.description) {
      for (const k of Object.keys(item.description)) {
        this.description.set(k, item.description[k]);
      }
    }

    this.displayName = new Map<string, string>();
    if (item.hasOwnProperty('displayName') && item.displayName) {
      for (const k of Object.keys(item.displayName)) {
        this.displayName.set(k.toLowerCase(), item.displayName[k]);
      }
    }

    this.required = item.required;
    this.displayed = item.displayed;
    this.editable = item.editable;
    this.type = item.type;
    this.allowedValues = item.allowedValues;
    this.displayPosition = item.displayPosition;
    this.regex = item.regex;
    this.allowedKeys = item.allowedKeys;
    if (item.hasOwnProperty('comment')) {
      this.comment = item.comment;
    }
    if (item.hasOwnProperty('oldValue')) {
      this.oldValue = item.oldValue;
    }
  }

  name: string;
  displayName: Map<string, string>;
  description: Map<string, string>;
  required: boolean;
  displayed: boolean;
  editable: boolean = false;
  type: string;
  allowedValues: string[];
  displayPosition: number;
  regex: string;
  allowedKeys: string[];
  comment: string;
  oldValue: any;

  public getLocalizedName(lang: string) {
    if (this.displayName.has(lang)) {
      return this.displayName.get(lang);
    } else {
      return this.displayName.get('en');
    }
  }

  public getLocalizedDescription(lang: string) {
    if (this.description.has(lang)) {
      return this.description.get(lang);
    } else {
      return this.description.get('en');
    }
  }

  public hasComment(): boolean {
    return this.comment !== undefined
      && this.comment !== null
      && this.comment.trim().length > 0;
  }

  public hasRegex(): boolean {
    return this.regex !== undefined &&
           this.regex !== null &&
           this.regex.trim().length > 0 &&
           this.regex !== 'URL'
  }
}
