import {Component, Input, ViewChild} from '@angular/core';
import {ApplicationItem} from "../../../../core/models/ApplicationItem";
import {faMinus, faPlus, faPlusCircle, faQuestion, faQuestionCircle} from "@fortawesome/free-solid-svg-icons";
import {RequestItem} from "../../RequestItem";
import {Attribute} from "../../../../core/models/Attribute";
import {MatFormField} from "@angular/material";
import {NgForm} from "@angular/forms";

@Component({
  selector: 'app-application-item-list',
  templateUrl: './application-item-list.component.html',
  styleUrls: ['./application-item-list.component.scss']
})
export class ApplicationItemListComponent implements RequestItem {

  constructor() { }

  removeIcon = faMinus;
  addIcon = faPlus;
  helpIcon = faQuestion;
  error = false;

  values : string[] = [];

  @Input()
  applicationItem: ApplicationItem;

  @ViewChild('form')
  form : NgForm;

  removeValue(index : number) {
    this.values.splice(index, 1);
  }

  addValue() {
    this.values.push("");
  }

  getAttribute(): Attribute {
    return new Attribute(this.applicationItem.name, this.values);
  }

  customTrackBy(index: number, obj: any): any {
    return index;
  }

  hasCorrectValue(): boolean {
    if (!this.applicationItem.required) {
      return true;
    } else {
      if (this.values.length === 0) {
        return false;
      }
    }

    for (let i = 0; i < this.values.length; i++) {
      if (this.values[i].trim().length === 0) {
        return false;
      }
    }

    return true;
  }

  onFormSubmitted(): void {
    if (!this.hasCorrectValue()) {
      for (let i = 0; i < this.values.length; i++) {
        let value = this.values[i];

        if (value.trim().length === 0) {
          this.form.form.controls['value-' + i].markAsTouched();
          this.form.form.controls['value-' + i].setErrors({'incorrect' : true});
          this.form.form.controls['value-' + i].updateValueAndValidity();
        }
      }
    }
  }
}
