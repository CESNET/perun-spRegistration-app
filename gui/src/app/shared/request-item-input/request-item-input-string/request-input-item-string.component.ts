import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {ApplicationItem} from '../../../core/models/ApplicationItem';
import {RequestItem} from '../../../core/models/RequestItem';
import {Attribute} from '../../../core/models/Attribute';
import {NgForm, NgModel} from '@angular/forms';

@Component({
  selector: 'request-item-input-string',
  templateUrl: './request-input-item-string.component.html',
  styleUrls: ['./request-input-item-string.component.scss']
})
export class RequestInputItemStringComponent implements RequestItem, OnInit {

  constructor() { }

  @Input()
  applicationItem: ApplicationItem;

  value: string = '';

  @ViewChild('form', {static: false})
  form: NgForm;

  @ViewChild('input', {static: false})
  inputField: NgModel;

  missingValueError = false;
  expectedValueChangedError = false;
  regexMismatchError = false;

  private static requestedChangeHasBeenMade(appItem: ApplicationItem, value: string): boolean {
    if (appItem.hasComment()) {
      return appItem.oldValue !== value;
    }

    return true;
  }

  private static hasValue(value: string): boolean {
    return value !== undefined && value !== null && value.trim().length > 0;
  }

  private static checkRegex(item: ApplicationItem, value: string): boolean {
    if (!item.hasRegex()) {
      return true;
    }

    return new RegExp(item.regex).test(value);
  }

  ngOnInit(): void {
    this.value = this.applicationItem.oldValue;
  }

  getAttribute(): Attribute {
    return new Attribute(this.applicationItem.name, this.value);
  }

  hasCorrectValue(): boolean {
    this.resetErrors();
    console.log(this.applicationItem.name);
    if (!RequestInputItemStringComponent.hasValue(this.value)) {
      if (this.applicationItem.required) {
        this.missingValueError = true;
        this.form.form.setErrors({'incorrect' : true});
        return false;
      }
    } else {
      if (!RequestInputItemStringComponent.checkRegex(this.applicationItem, this.value)) {
        this.form.form.setErrors({'incorrect' : true});
        this.regexMismatchError = true;
        return false;
      }

      if (!RequestInputItemStringComponent.requestedChangeHasBeenMade(this.applicationItem, this.value)) {
        this.form.form.setErrors({'incorrect' : true});
        this.expectedValueChangedError = true;
        return false;
      }

      return this.inputField.valid;
    }

    return this.inputField.valid;
  }

  onFormSubmitted(): void {
    if (this.value) {
      this.value = this.value.trim();
    }
    if (!this.hasCorrectValue()) {
      this.form.form.controls[this.applicationItem.name].markAsTouched();
      this.form.form.controls[this.applicationItem.name].setErrors({'incorrect' : true});
      this.form.form.controls[this.applicationItem.name].updateValueAndValidity();
    }
  }

  hasError(): boolean {
    return this.expectedValueChangedError ||
           this.missingValueError ||
           this.regexMismatchError;
  }

  private resetErrors(): void {
    this.expectedValueChangedError = false;
    this.regexMismatchError = false;
    this.missingValueError = false;
  }

}
