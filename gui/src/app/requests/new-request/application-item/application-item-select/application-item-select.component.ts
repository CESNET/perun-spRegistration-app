import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {faMinus, faPlus, faQuestionCircle} from "@fortawesome/free-solid-svg-icons";
import {ApplicationItem} from "../../../../core/models/ApplicationItem";
import {Attribute} from "../../../../core/models/Attribute";
import {RequestItem} from "../../RequestItem";
import {NgForm} from "@angular/forms";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'app-application-item-select',
  templateUrl: './application-item-select.component.html',
  styleUrls: ['./application-item-select.component.scss']
})
export class ApplicationItemSelectComponent implements RequestItem, OnInit {

  constructor(private translate: TranslateService) { }

  removeIcon = faMinus;
  addIcon = faPlus;
  helpIcon = faQuestionCircle;

  values = [];
  translatedName: string;
  translatedDescription: string;

  @Input()
  applicationItem: ApplicationItem;

  @ViewChild('form')
  form : NgForm;

  getAttribute(): Attribute {
    return new Attribute(this.applicationItem.name, this.values);
  }

  hasCorrectValue(): boolean {
    if (!this.applicationItem.required) {
      return true;
    }

    return this.values.length > 0;
  }

  onFormSubmitted(): void {
    if (!this.hasCorrectValue()) {
      this.form.form.controls[this.applicationItem.name].markAsTouched();
      this.form.form.controls[this.applicationItem.name].setErrors({'incorrect' : true});
    }
  }

  ngOnInit(): void {
    let browserLang = this.translate.getDefaultLang();
    this.translatedDescription = this.applicationItem.description[browserLang];
    this.translatedName = this.applicationItem.displayName[browserLang];
  }
}
