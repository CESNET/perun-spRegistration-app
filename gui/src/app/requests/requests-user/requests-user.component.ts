import { Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import { RequestsService } from '../../core/services/requests.service';
import { Subscription } from 'rxjs';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {MatPaginator} from '@angular/material/paginator';
import { TranslateService } from "@ngx-translate/core";
import {RequestOverview} from "../../core/models/RequestOverview";

@Component({
  selector: 'app-requests-overview',
  templateUrl: './requests-user.component.html',
  styleUrls: ['./requests-user.component.scss']
})
export class RequestsUserComponent implements OnInit, OnDestroy {

  private requestsSubscription: Subscription;
  private sort: MatSort;
  private paginator: MatPaginator;

  constructor(
    private requestsService: RequestsService,
    private translate: TranslateService
  ) {
    this.setDataSource();
  }

  @ViewChild(MatSort, {static: false}) set matSort(ms: MatSort) {
    this.sort = ms;
    this.setDataSource();
  }

  @ViewChild(MatPaginator, {static: false}) set matPaginator(mp: MatPaginator) {
    this.paginator = mp;
    this.setDataSource();
  }

  loading = true;
  displayedColumns: string[] = RequestOverview.columns

  requests: RequestOverview[] = [];
  dataSource: MatTableDataSource<RequestOverview> = new MatTableDataSource<RequestOverview>(this.requests);

  ngOnInit() {
    this.requestsSubscription = this.requestsService.getUserRequests().subscribe(requests => {
      this.requests = requests.map(r => new RequestOverview(r));
      this.setDataSource();
      this.loading = false;
    }, error => {
      this.loading = false;
      //TODO: display error to user
      console.log(error);
    });
  }

  ngOnDestroy() {
    this.requestsSubscription.unsubscribe();
  }

  doFilter(value: string) {
    this.dataSource.filter = value.trim().toLowerCase();
  }

  setDataSource() {
    this.dataSource = new MatTableDataSource<RequestOverview>(this.requests);
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.setRequestOverviewSorting();
    this.setRequestOverviewFiltering();
  }

  private setRequestOverviewSorting() {
    this.dataSource.sortingDataAccessor = ((data, sortHeaderId) => {
      switch (sortHeaderId) {
        case 'id':
          return data.id;
        case 'serviceIdentifier': {
          return data.serviceIdentifier;
        }
        case 'serviceName': {
          let name = '';
          if (data.serviceName && data.serviceName.has(this.translate.currentLang)) {
            name = data.serviceName.get(this.translate.currentLang).toLowerCase();
          }
          return name
        }
        case 'requesterId':
          return data.requesterId;
        case 'status':
          return data.status;
        case 'action':
          return data.action;
      }
    });
  }

  private setRequestOverviewFiltering() {
    this.dataSource.filterPredicate = ((data: RequestOverview, filter: string) => {
      const id = data.id ? data.id.toString(): '';
      let name = '';
      if (data.serviceName && data.serviceName.has(this.translate.currentLang)) {
        name = data.serviceName.get(this.translate.currentLang).replace(/\s/g, '').toLowerCase();
      }
      const action = data.action.toString().replace('_', ' ').toLowerCase();
      const status = data.status.toString().replace('_', ' ').toLowerCase();
      const serviceIdentifier = data.serviceIdentifier.toLowerCase();
      const requesterId = data.requesterId.toString();

      return id.includes(filter) || name.includes(filter) || serviceIdentifier.includes(filter)
        || action.includes(filter) || status.includes(filter) || requesterId.includes(filter);
    });
  }

}
