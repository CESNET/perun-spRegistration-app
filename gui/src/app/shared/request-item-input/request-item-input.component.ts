import {AfterViewInit, Component, Input, ViewChild} from '@angular/core';
import {ApplicationItem} from '../../core/models/ApplicationItem';
import {RequestItem} from '../../core/models/RequestItem';
import {Attribute} from '../../core/models/Attribute';
import {RequestInputItemStringComponent} from './request-item-input-string/request-input-item-string.component';
import {RequestItemInputBooleanComponent} from './request-item-input-boolean/request-item-input-boolean.component';
import {RequestItemInputListComponent} from './request-item-input-list/request-item-input-list.component';
import {RequestItemInputMapComponent} from './request-item-input-map/request-item-input-map.component';
import {RequestItemInputSelectComponent} from './request-item-input-select/request-item-input-select.component';

@Component({
  selector: 'request-input-item',
  templateUrl: './request-item-input.component.html',
  styleUrls: ['./request-item-input.component.scss']
})
export class RequestItemInputComponent implements RequestItem, AfterViewInit {

  constructor() { }

  @Input() applicationItem: ApplicationItem;
  @ViewChild(RequestInputItemStringComponent, {static: false}) stringItem: RequestItem;
  @ViewChild(RequestItemInputBooleanComponent, {static: false}) booleanItem: RequestItem;
  @ViewChild(RequestItemInputListComponent, {static: false}) listItem: RequestItem;
  @ViewChild(RequestItemInputMapComponent, {static: false}) mapItem: RequestItem;
  @ViewChild(RequestItemInputSelectComponent, {static: false}) selectItem: RequestItem;

  item: RequestItem;

  getAttribute(): Attribute {
    return this.item.getAttribute();
  }

  hasCorrectValue(): boolean {
    return this.item.hasCorrectValue();
  }

  ngAfterViewInit(): void {
    if (this.applicationItem.type === 'java.lang.String' || this.applicationItem.type === 'java.lang.LargeString') {
      this.item = this.stringItem;
    } else if (this.applicationItem.type === 'java.lang.Boolean') {
      this.item = this.booleanItem;
    } else if ((this.applicationItem.type === 'java.util.ArrayList' || this.applicationItem.type === 'java.util.LargeArrayList')
        && (this.applicationItem.allowedValues === null || this.applicationItem.allowedValues.length === 0))
    {
      this.item = this.listItem;
    } else if ((this.applicationItem.type === 'java.util.ArrayList' || this.applicationItem.type === 'java.util.LargeArrayList')
        && this.applicationItem.allowedValues.length > 0)
    {
      this.item = this.selectItem;
    } else if (this.applicationItem.type === 'java.util.LinkedHashMap') {
      this.item = this.mapItem;
    } else {
      console.log('Did not find item', this.applicationItem);
    }
  }

  onFormSubmitted(): void {
    console.log(this.applicationItem);
    this.item.onFormSubmitted();
  }

}
