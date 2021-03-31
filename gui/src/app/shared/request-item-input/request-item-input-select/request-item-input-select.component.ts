import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {ApplicationItem} from '../../../core/models/ApplicationItem';
import {Attribute} from '../../../core/models/Attribute';
import {RequestItem} from '../../../core/models/RequestItem';
import {NgForm} from '@angular/forms';
import {RequestItemInputUtils} from "../request-item-input-utils/request-item-input.component";

@Component({
  selector: 'request-item-input-select',
  templateUrl: './request-item-input-select.component.html',
  styleUrls: ['./request-item-input-select.component.scss']
})
export class RequestItemInputSelectComponent implements RequestItem, OnInit {

  constructor() { }

  values: string[] = [];

  @Input()
  applicationItem: ApplicationItem;

  @ViewChild('form', {static: false})
  form: NgForm;

  missingValueError = false;
  expectedValueChangedError = false;

  private static hasValue(values: string[]): boolean {
    return values !== undefined && values !== null && values.length > 0;
  }

  ngOnInit(): void {
    if (this.applicationItem.oldValue != null) {
      const array: string[] = this.applicationItem.oldValue;
      for (const value of array) {
        this.values.push(value);
      }
    }
  }

  getAttribute(): Attribute {
    return new Attribute(this.applicationItem.name, this.values);
  }

  hasCorrectValue(): boolean {
    this.resetErrors();
    if (!RequestItemInputSelectComponent.hasValue(this.values)) {
      if (this.applicationItem.required) {
        this.form.form.setErrors({'incorrect' : true});
        this.missingValueError = true;
        return false;
      }
    } else {
      if (!RequestItemInputUtils.requestedChangeHasBeenMadeMultiValue(this.applicationItem, this.values)) {
        this.form.form.setErrors({'incorrect' : true});
        this.expectedValueChangedError = true;
        return false;
      }
    }

    return true;
  }

  onFormSubmitted(): void {
    if (!this.hasCorrectValue()) {
      this.form.form.controls[this.applicationItem.name].markAsTouched();
      this.form.form.controls[this.applicationItem.name].setErrors({'incorrect' : true});
      this.form.form.controls[this.applicationItem.name].updateValueAndValidity();
    }
  }

  hasError(): boolean {
    return this.expectedValueChangedError ||
      this.missingValueError;
  }

  private resetErrors(): void {
    this.expectedValueChangedError = false;
    this.missingValueError = false;
  }

}
