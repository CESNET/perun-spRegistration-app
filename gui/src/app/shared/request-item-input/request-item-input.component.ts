import { AfterViewInit, Component, Input, ViewChild } from '@angular/core';
import { ApplicationItem } from '../../core/models/ApplicationItem';
import { RequestItem } from '../../core/models/RequestItem';
import { Attribute } from '../../core/models/Attribute';
import { RequestInputItemStringComponent } from './request-item-input-string/request-input-item-string.component';
import { RequestItemInputBooleanComponent } from './request-item-input-boolean/request-item-input-boolean.component';
import { RequestItemInputListComponent } from './request-item-input-list/request-item-input-list.component';
import { RequestItemInputMapComponent } from './request-item-input-map/request-item-input-map.component';
import { RequestItemInputMultiselectComponent } from './request-item-input-multiselect/request-item-input-multiselect.component';
import { RequestInputItemSelectComponent } from './request-item-input-select/request-input-item-select.component';

@Component({
  selector: 'request-input-item',
  templateUrl: './request-item-input.component.html',
  styleUrls: ['./request-item-input.component.scss'],
})
export class RequestItemInputComponent implements RequestItem, AfterViewInit {
  constructor() {}

  @Input() newApp = false;
  @Input() applicationItem: ApplicationItem;
  @ViewChild(RequestInputItemStringComponent, { static: false })
  stringItem: RequestItem;

  @ViewChild(RequestInputItemSelectComponent, { static: false })
  selectItem: RequestItem;

  @ViewChild(RequestItemInputBooleanComponent, { static: false })
  booleanItem: RequestItem;

  @ViewChild(RequestItemInputListComponent, { static: false })
  listItem: RequestItem;

  @ViewChild(RequestItemInputMapComponent, { static: false })
  mapItem: RequestItem;

  @ViewChild(RequestItemInputMultiselectComponent, { static: false })
  multiSelectItem: RequestItem;

  item: RequestItem;

  getAttribute(): Attribute {
    return this.item.getAttribute();
  }

  hasCorrectValue(): boolean {
    return this.item.hasCorrectValue();
  }

  ngAfterViewInit(): void {
    switch (this.applicationItem.type) {
      case 'java.lang.String':
        {
          if (!this.applicationItem.isSelect()) {
            this.item = this.stringItem;
          } else {
            this.item = this.selectItem;
          }
        }
        break;
      case 'java.lang.Boolean':
        {
          this.item = this.booleanItem;
        }
        break;
      case 'java.util.ArrayList':
        {
          if (!this.applicationItem.isSelect()) {
            this.item = this.listItem;
          } else {
            this.item = this.multiSelectItem;
          }
        }
        break;
      case 'java.util.LinkedHashMap':
        {
          this.item = this.mapItem;
        }
        break;
      default: {
        console.log('Did not find item', this.applicationItem);
      }
    }
  }

  onFormSubmitted(): void {
    console.log(this.applicationItem);
    this.item.onFormSubmitted();
  }
}
