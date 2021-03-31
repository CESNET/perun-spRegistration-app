import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {ApplicationItem} from '../../../core/models/ApplicationItem';
import {RequestItem} from '../../../core/models/RequestItem';
import {Attribute} from '../../../core/models/Attribute';
import {NgForm} from '@angular/forms';

@Component({
  selector: 'request-input-item-boolean',
  templateUrl: './request-item-input-boolean.component.html',
  styleUrls: ['./request-item-input-boolean.component.scss']
})
export class RequestItemInputBooleanComponent implements OnInit, RequestItem {

  constructor() { }

  @Input()
  applicationItem: ApplicationItem;

  @ViewChild('form', {static: false})
  form: NgForm;

  value: boolean = false;
  expectedValueChangedError = false;

  ngOnInit(): void {
    this.value = this.applicationItem.oldValue;
  }

  getAttribute(): Attribute {
    return new Attribute(this.applicationItem.name, this.value);
  }

  hasCorrectValue(): boolean {
    if (this.applicationItem.hasComment()) {
      if (this.applicationItem.oldValue === this.value) {
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
    return this.expectedValueChangedError;
  }

}
