import {ApplicationItem} from "../../../core/models/ApplicationItem";

export class RequestItemInputUtils {

  constructor() { }

  public static hasValue(values: string[]): boolean {
    if (values === undefined || values === null || values.length === 0) {
      return false;
    }

    for (let i = 0; i < values.length; i++) {
      const subValue = values[i];
      if (subValue === undefined || subValue === null || subValue.length === 0) {
        return false;
      }
    }

    return true;
  }

  public static requestedChangeHasBeenMade(appItem: ApplicationItem, values: string[]): boolean {
    if (appItem.hasComment()) {
      if (appItem.oldValue.length !== values.length) {
        return true;
      }
      for (let i = 0; i < values.length; i++) {
        if (appItem.oldValue.indexOf(values[i]) === -1) {
          return true;
        }
      }
      return false;
    }
    return true;
  }

  public static requestedChangeHasBeenMadeSingleValue(appItem: ApplicationItem, value: string): boolean {
    if (appItem.hasComment()) {
      if (appItem.oldValue === value) {
        return false;
      }
    }
    return true;
  }

  public static checkRegex(item: ApplicationItem, values: string[]): number[] {
    const indexes = [];
    if (item.hasRegex()) {
      const reg = new RegExp(item.regex);
      for (let i = 0; i < values.length; i++) {
        if (!reg.test(values[i])) {
          indexes.push(i);
        }
      }
    }

    return indexes;
  }

  public static checkRegexSingleValue(item: ApplicationItem, value: string): boolean {
    if (!item.hasRegex()) {
      return true;
    }
    return new RegExp(item.regex).test(value);
  }

}
