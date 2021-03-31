import {PerunAttribute} from '../PerunAttribute';

export class DetailViewItem {

  constructor(attr: PerunAttribute) {
    if (!attr) {
      return;
    }

    this.urn = attr.fullName;
    this.value = attr.value;
    this.oldValue = attr.hasOwnProperty('oldValue') ? attr.oldValue : null;
    this.comment = attr.hasOwnProperty('comment') ? attr.comment : null;
    this.type = attr.definition.type;
    this.name = new Map<string, string>();
    this.description = new Map<string, string>();

    if (attr.input) {
      if (attr.input.displayName) {
        attr.input.displayName.forEach((value: string, key: string) => {
          this.name.set(key.toLowerCase(), value);
        });
      } else {
        this.name.set('en', attr.definition.displayName);
      }
      if (attr.input.description) {
        attr.input.description.forEach((value: string, key: string) => {
          this.description.set(key.toLowerCase(), value);
        });
      } else {
        this.description.set('en', attr.definition.description);
      }
      this.position = attr.input.displayPosition;
    } else {
      this.name.set('en', attr.definition.displayName);
      this.description.set('en', attr.definition.description);
      this.position = 0;
    }
  }

  urn: string;
  value: any;
  oldValue: any;
  name: Map<string, string>;
  comment: string;
  description: Map<string, string>;
  position: number;
  type: string;

  public hasValueChanged(): boolean {
    switch (this.type) {
      case 'java.lang.Boolean': {
        return this.oldValue !== undefined
          && this.oldValue !== null
          && this.oldValue !== this.value;
      }
      case 'java.lang.String':
      case 'java.lang.LargeString' :
      case 'java.lang.Integer':
        return this.oldValue !== this.value;
      case 'java.util.ArrayList':
      case 'java.util.LargeArrayList':
        return !DetailViewItem.arrayValuesAreEqual(this.value, this.oldValue);
      case 'java.util.LinkedHashMap':
        return !DetailViewItem.mapValuesAreEqual(this.value, this.oldValue);
    }
    return false;
  }

  private static arrayValuesAreEqual(v1: string[], v2: string[]) {
    if (v1 === undefined) {
      return v2 === undefined;
    } else if (v1 === null) {
      return v2 === null;
    } else if (v2 === undefined || v2 === null) {
      return false;
    } else if (v1.length !== v2.length) {
      return false;
    }

    v1 = v1.sort();
    v2 = v2.sort();
    let result = true;
    for (let i = 0; i < v1.length; i++) {
      if (v1[i] !== v2[i]) {
        result = false;
      }
    }
    return result;
  }

  private static mapValuesAreEqual(v1: Object, v2: Object) {
    if (v1 === undefined) {
      return v2 === undefined;
    } else if (v1 === null) {
      return v2 === null;
    } else if (v2 === undefined || v2 === null) {
      return false;
    } else if (Object.keys(v1).length !== Object.keys(v2).length) {
      return false;
    }

    let result = true;
    Object.keys(v1).forEach(function(key) {
      const value = v1[key];
      if (!v2.hasOwnProperty(key)) {
        result = false;
      } else if (value !== v2[key]) {
        result = false;
      }
    });
    return result;
  }

}
