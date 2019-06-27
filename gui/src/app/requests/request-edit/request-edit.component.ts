import {Component, OnInit, QueryList, ViewChildren} from '@angular/core';
import {ConfigService} from "../../core/services/config.service";
import {ApplicationItem} from "../../core/models/ApplicationItem";
import {RequestsService} from "../../core/services/requests.service";
import {MatSnackBar} from "@angular/material";
import {TranslateService} from "@ngx-translate/core";
import {PerunAttribute} from "../../core/models/PerunAttribute";
import {ActivatedRoute, Router} from "@angular/router";
import {Request} from "../../core/models/Request";
import {ApplicationItemComponent} from "../new-request/application-item/application-item.component";

@Component({
  selector: 'app-request-edit',
  templateUrl: './request-edit.component.html',
  styleUrls: ['./request-edit.component.scss']
})
export class RequestEditComponent implements OnInit {

  constructor(
    private route: ActivatedRoute,
    private configService: ConfigService,
    private requestsService: RequestsService,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
    private router: Router) {
  }

  @ViewChildren(ApplicationItemComponent)
  items: QueryList<ApplicationItemComponent>;

  private sub: any;
  isFormVisible = false;
  isCardBodyVisible = false;
  loading = true;
  snackBarDurationMs = 8000;
  showMore: boolean = false;

  request: Request;

  // translations
  errorText: string;
  successfullySubmittedText: string;
  successActionText: string;

  applicationItemGroupsWithComment: ApplicationItem[][];
  applicationItemGroupsWithoutComment: ApplicationItem[][];

  ngOnInit() {
    this.translate.get('REQUESTS.NEW_VALUES_ERROR_MESSAGE')
      .subscribe(value => this.errorText = value);
    this.translate.get('REQUESTS.SUCCESSFULLY_SUBMITTED_MESSAGE')
      .subscribe(value => this.successfullySubmittedText = value);
    this.translate.get('REQUESTS.SUCCESSFULLY_SUBMITTED_ACTION')
      .subscribe(value => this.successActionText = value);
    this.getAttributes();
  }


  revealForm() {
    this.loading = false;
    this.isCardBodyVisible = true;
    this.isFormVisible = true;
  }


  onLoading() {
    this.loading = true;
    this.isCardBodyVisible = false;
  }


  submitRequest() {

    let perunAttributes: PerunAttribute[] = [];
    
    this.items.forEach(i => {
      let attr = i.getAttribute();
      let perunAttr = new PerunAttribute(attr.value, attr.urn);
      perunAttributes.push(perunAttr);
    });

    console.log(perunAttributes);

    this.requestsService.updateRequest(this.request.reqId, perunAttributes).subscribe(boolean => {
      let snackBarRef = this.snackBar
        .open(this.successfullySubmittedText, this.successActionText, {duration: this.snackBarDurationMs});
      snackBarRef.onAction().subscribe(() => {
        this.router.navigate(['/auth/requests/detail/' + this.request.reqId]);
      });
      this.router.navigate(['/auth']);
    });
  }


  private static filterItems(items: ApplicationItem[][]): ApplicationItem[][] {
    let filteredItems: ApplicationItem[][] = [];

    items.forEach(itemsGroup => {
      let filteredGroup: ApplicationItem[] = [];

      itemsGroup.forEach(item => {
        if (item.displayed) {
          filteredGroup.push(item);
        }
      });

      filteredItems.push(filteredGroup);
    });

    return filteredItems;
  }


  private static sortItems(items: ApplicationItem[][]): ApplicationItem[][] {
    let sortedItems: ApplicationItem[][] = [];

    items.forEach(itemsGroup => {
      sortedItems.push(itemsGroup.sort(((a, b) => {
        return a.displayPosition - b.displayPosition;
      })))
    });

    console.log(sortedItems);
    return sortedItems;
  }

  private getAttributes(): void {
    this.sub = this.route.params.subscribe(params => {
      this.requestsService.getRequest(params['id']).subscribe(request => {
        this.request = request;

        this.applicationItemGroupsWithComment = [[]];
        this.applicationItemGroupsWithoutComment = [[]];
        for(const [key, value] of Object.entries(this.request.attributes)) {
          value.input.oldValue = value.value;
          if (value.comment != null){
            value.input.comment = value.comment;
            this.applicationItemGroupsWithComment[0].push(value.input);
            value.input.isEdit = true;
          } else {
            this.applicationItemGroupsWithoutComment[0].push(value.input);
            value.input.isEdit = false;
          }
        }

        this.applicationItemGroupsWithComment = RequestEditComponent.sortItems(RequestEditComponent.filterItems(this.applicationItemGroupsWithComment));
        this.revealForm();

        this.loading = false;
      }, error => {
        this.loading = false;
        console.log(error);
      });
    });
  }

  private changeShowMore(){
    this.showMore = !this.showMore;
    this.ngOnInit();
  }
}
