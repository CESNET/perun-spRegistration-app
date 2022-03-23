import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {ApplicationItem} from '../../../core/models/ApplicationItem';
import {Attribute} from '../../../core/models/Attribute';
import {RequestItem} from '../../../core/models/RequestItem';
import {NgForm} from '@angular/forms';
import {RequestItemInputUtils} from "../request-item-input-utils/request-item-input.component";

@Component({
  selector: 'request-item-input-multiselect',
  templateUrl: './request-item-input-multiselect.component.html',
  styleUrls: ['./request-item-input-multiselect.component.scss']
})
export class RequestItemInputMultiselectComponent implements RequestItem, OnInit {

  constructor() { }

  @Input() newApp: boolean = false;
  @Input() applicationItem: ApplicationItem;
  @ViewChild('form', {static: false}) form: NgForm;

  values: string[] = [];

  missingValueError = false;
  expectedValueChangedError = false;

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
    if (!this.newApp && !this.checkChangeMade()) {
      return false;
    }

    if (!RequestItemInputUtils.hasValue(this.values)) {
      return this.checkValueRequired();
    } else {
      return true;
    }
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

  private checkValueRequired(): boolean {
    if (this.applicationItem.required) {
      this.form.form.setErrors({'incorrect' : true});
      this.missingValueError = true;
      return false;
    }
    return true;
  }

  private checkChangeMade(): boolean {
    if (!RequestItemInputUtils.requestedChangeHasBeenMade(this.applicationItem, this.values)) {
      this.form.form.setErrors({'incorrect' : true});
      this.expectedValueChangedError = true;
      return false;
    }
    return true;
  }

}
