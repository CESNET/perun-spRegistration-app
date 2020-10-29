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

    this.name = new Map<string, string>();
    this.description = new Map<string, string>();

    if (attr.input) {
      this.position = attr.input.displayPosition;

      if (attr.input.displayName) {
        this.name.set('en', attr.input.displayName.get('en'));
        if (attr.input.displayName.get('cs')) {
          this.name.set('cs', attr.input.displayName.get('cs'));
        }
      } else {
        this.name.set('en', attr.definition.displayName);
      }

      if (attr.input.description) {
        this.description.set('en', attr.input.description.get('en'));
        if (attr.input.displayName.get('cs')) {
          this.description.set('cs', attr.input.description.get('cs'));
        }
      } else {
        this.description.set('en', attr.definition.description);
      }
    } else {
      this.position = 0;
    }

    this.type = attr.definition.type;
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
        return !DetailViewItem.mapValuesEqual(this.value, this.oldValue);
    }
    return false;
  }

  public hasOldValue(): boolean {
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
        return !DetailViewItem.mapValuesEqual(this.value, this.oldValue);
    }
    return false;
  }

  private static arrayValuesAreEqual(v1: string[], v2: string[]) {
    if (v1 === undefined) {
      return v2 === undefined;
    } else if (v1 === null) {
      return v2 === null;
    }

    if (v2 === undefined || v2 === null) {
      return false;
    }

    if (v1.length !== v2.length) {
      return false;
    }

    v1.sort().forEach(el => {
      if (v2.indexOf(el) === -1) {
        return false;
      }
    });
    return true;
  }

  private static mapValuesEqual(v1: Map<string, string>, v2: Map<string, string>) {
    if (v1 === undefined) {
      return v2 === undefined;
    } else if (v1 === null) {
      return v2 === null;
    }

    if (v2 === undefined || v2 === null) {
      return false;
    }

    if (Object.keys(v1).length !== Object.keys(v2).length) {
      return false;
    }

    Object.keys(v1).forEach(function (key) {
      const value = v1[key];
      if (!v2.hasOwnProperty(key)) {
        return false;
      } else if (value !== v2[key]) {
        return false;
      }
      return true;
    });
  }

}
