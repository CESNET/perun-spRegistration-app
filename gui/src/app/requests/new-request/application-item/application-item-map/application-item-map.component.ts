import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {ApplicationItem} from "../../../../core/models/ApplicationItem";
import {RequestItem} from "../../RequestItem";
import {Attribute} from "../../../../core/models/Attribute";
import {NgForm} from "@angular/forms";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'app-application-item-map',
  templateUrl: './application-item-map.component.html',
  styleUrls: ['./application-item-map.component.scss']
})
export class ApplicationItemMapComponent implements RequestItem, OnInit {

  constructor(private translate: TranslateService) {
  }

  keys: string[] = [];
  values: string[] = [];
  indexes: number[] = [];

  private index = 0;

  noItemError = false;
  duplicitKeysError = false;
  noValueError = false;

  translatedName: string;
  translatedDescription: string;

  disableCustomKeys = false;

  @Input()
  applicationItem: ApplicationItem;

  @ViewChild('form')
  form: NgForm;

  removeValue(index: number) {
    this.values.splice(index, 1);
    this.keys.splice(index, 1);
    this.indexes.splice(index, 1);

    if (this.values.length === 0) {
      this.noItemError = true;
    }
  }

  addValue() {
    this.values.push("");
    this.keys.push("");
    this.indexes.push(this.index++);
    this.noItemError = false;
  }

  addValueNonEmpty(key: string, value: string){
    this.values.push(value);
    this.keys.push(key);
    this.indexes.push(this.index++);
    this.noItemError = false;
  }

  getAttribute(): Attribute {
    let map = new Map();

    for (let i = 0; i < this.values.length; i++) {
      map.set(this.keys[i], this.values[i]);
    }

    const obj = Array.from(map.entries()).reduce((main, [key, value]) => ({...main, [key]: value}), {});

    return new Attribute(this.applicationItem.name, obj);
  }

  customTrackBy(index: number, obj: any): any {
    return index;
  }

  private isFilledAtLeastOneValue(): boolean {
    for (const value of this.values) {
      if (value.trim().length > 0) {
        return true;
      }
    }

    return false;
  }

  hasCorrectValue(): boolean {
    // reset errors
    this.duplicitKeysError = false;
    this.noValueError = false;

    console.log(this.values);

    if (!this.applicationItem.required && this.values.length === 0) {
      return true;
    } else {
      if (this.disableCustomKeys && !this.isFilledAtLeastOneValue()) {
        this.noValueError = true;
        return false;
      }
      else if (this.values.length === 0) {
        return false;
      }
    }

    let keysWithIndexes = new Map<string, number>();

    for (let i = 0; i < this.values.length; i++) {
      let keys = Array.from(keysWithIndexes.keys());

      let value = this.values[i].trim();
      let key = this.keys[i].trim();

      if (keys.includes(key)) {
        this.duplicitKeysError = true;
        this.showErredKey(keysWithIndexes.get(key));
        this.showErredKey(i);

        return false;
      }

      keysWithIndexes.set(key, i);
    }

    return true;
  }

  showErredKey(orderNumber: number) {
    let index = this.indexes[orderNumber];
    let input = this.form.form.controls['key-' + index];

    input.markAsTouched();
    input.setErrors({'incorrect': true});
  }

  showErredValue(orderNumber: number) {
    let index = this.indexes[orderNumber];
    let input = this.form.form.controls['value-' + index];

    input.markAsTouched();
    input.setErrors({'incorrect': true});
  }

  ngOnInit(): void {
    let browserLang = this.translate.getDefaultLang();
    this.translatedDescription = this.applicationItem.description[browserLang];
    this.translatedName = this.applicationItem.displayName[browserLang];

    //if (this.applicationItem.isEdit) {
    if (this.applicationItem.oldValue != null){
      let map: Map<string, string> = this.applicationItem.oldValue;
      for (const [key, value] of Object.entries(map)) {
        this.addValueNonEmpty(key, value);
      }
    } else {
      if (this.applicationItem.allowedKeys != undefined && this.applicationItem.allowedKeys.length > 0) {
        this.disableCustomKeys = true;

        this.keys = this.applicationItem.allowedKeys;
        let map: Map<string, string> = this.applicationItem.oldValue;

        //this.values.push(map.values());
        for (let i = 0; i < this.keys.length; i++) {
          //this.values.push(map.get());
          this.values.push("");
          this.indexes.push(this.index++);
        }
        this.noItemError = false;
      }
    }
  }

  onFormSubmitted(): void {
    if (!this.hasCorrectValue()) {
      if (this.values.length === 0) {
        console.log("ERRED: " + this.applicationItem.displayName);
        this.noItemError = true;
      }
    }
  }
}
