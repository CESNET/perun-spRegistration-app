import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {ApplicationItem} from '../../../core/models/ApplicationItem';
import {RequestItem} from '../../../core/models/RequestItem';
import {Attribute} from '../../../core/models/Attribute';
import {NgForm} from '@angular/forms';
import {RequestItemInputUtils} from "../request-item-input-utils/request-item-input.component";

@Component({
  selector: 'request-item-input-list',
  templateUrl: './request-item-input-list.component.html',
  styleUrls: ['./request-item-input-list.component.scss']
})
export class RequestItemInputListComponent implements RequestItem, OnInit {

  constructor() { }

  values: string[] = [];
  error = false;
  missingValueError = false;
  expectedValueChangedError = false;
  regexMismatchError = false;

  @Input()
  applicationItem: ApplicationItem;

  @ViewChild('form', {static: false})
  form: NgForm;

  ngOnInit(): void {
    if (this.applicationItem.oldValue != null) {
      const array: string[] = this.applicationItem.oldValue;
      for (const value of array) {
        this.values.push(value);
      }
    }

    if (this.applicationItem.required && this.values.length === 0) {
      this.addValue();
    }
  }

  removeValue(index: number) {
    this.values.splice(index, 1);
  }

  addValue() {
    this.values.push('');
  }

  getAttribute(): Attribute {
    return new Attribute(this.applicationItem.name, this.values);
  }

  customTrackBy(index: number, _: any): any {
    return index;
  }

  hasCorrectValue(): boolean {
    this.resetErrors();

    if (!RequestItemInputUtils.hasValueMultiValue(this.values)) {
      if (this.applicationItem.required) {
        this.form.form.setErrors({'incorrect' : true});
        this.missingValueError = true;
        return false;
      }
    } else {
      const errIndexes = RequestItemInputUtils.checkRegexMultiValue(this.applicationItem, this.values);
      if (errIndexes.length > 0) {
        this.form.form.setErrors({'incorrect' : true});
        this.showErredValues(errIndexes);
        this.regexMismatchError = true;
        return false;
      }

      if (!RequestItemInputUtils.requestedChangeHasBeenMadeMultiValue(this.applicationItem, this.values)) {
        this.form.form.setErrors({'incorrect' : true});
        this.expectedValueChangedError = true;
        return false;
      }
    }

    return true;
  }

  showErredValues(errIndexes: number[]) {
    for (let i = 0; i < errIndexes.length; i++) {
      const input = this.form.form.controls['value-' + i];

      input.markAsTouched();
      input.setErrors({'incorrect': true});
    }
  }

  onFormSubmitted(): void {
    if (!this.hasCorrectValue()) {
      for (let i = 0; i < this.values.length; i++) {
        const value = this.values[i];

        if (value === undefined || value === null || value.trim().length === 0) {
          this.form.form.controls['value-' + i].markAsTouched();
          this.form.form.controls['value-' + i].setErrors({'incorrect' : true});
          this.form.form.controls['value-' + i].updateValueAndValidity();
        }
      }
    }
  }

  hasError(): boolean {
    return this.missingValueError || this.missingValueError || this.regexMismatchError;
  }

  private resetErrors(): void {
    this.expectedValueChangedError = false;
    this.regexMismatchError = false;
    this.missingValueError = false;
  }
}
