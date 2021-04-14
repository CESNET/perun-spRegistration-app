import { Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import { Request } from '../../core/models/Request';
import { RequestsService } from '../../core/services/requests.service';
import { Subscription } from 'rxjs';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {MatPaginator} from '@angular/material/paginator';
import { TranslateService } from "@ngx-translate/core";

@Component({
  selector: 'app-requests-overview',
  templateUrl: './requests-overview.component.html',
  styleUrls: ['./requests-overview.component.scss']
})
export class RequestsOverviewComponent implements OnInit, OnDestroy {

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
  displayedColumns: string[] = ['id', 'serviceName', 'reqUser', 'status', 'action'];
  requests: Request[] = [];
  dataSource: MatTableDataSource<Request> = new MatTableDataSource<Request>(this.requests);

  ngOnInit() {
    this.requestsSubscription = this.requestsService.getUserRequests().subscribe(requests => {
      this.requests = requests.map(r => new Request(r));
      this.setDataSource();
      this.loading = false;
    }, error => {
      this.loading = false;
      console.log(error);
    });
  }

  ngOnDestroy() {
    this.requestsSubscription.unsubscribe();
  }

  doFilter(value: string) {
    this.dataSource.filter = value.trim().toLowerCase();
    console.log(this.dataSource.filteredData);
  }

  setDataSource() {
    this.dataSource = new MatTableDataSource<Request>(this.requests);
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.setSorting();
    this.setFiltering();
  }

  private setSorting() {
    this.dataSource.sortingDataAccessor = ((data, sortHeaderId) => {
      switch (sortHeaderId) {
        case 'id':
          return data.reqId;
        case 'reqUser': {
          return data.reqUserId;
        }
        case 'serviceName': {
          const name = data.providedService ? data.providedService.name : null;
          if (!!name && name.has(this.translate.currentLang)) {
            return name.get(this.translate.currentLang).toLowerCase();
          } else {
            return "";
          }
        }
        case 'status':
          return data.status;
        case 'action':
          return data.action;
      }
    });
  }

  private setFiltering() {
    this.dataSource.filterPredicate = ((data: Request, filter: string) => {
      const reqId = data.reqId.toString();
      const facilityId = data.facilityId ? data.facilityId.toString() : '';
      const origName = data.providedService ? data.providedService.name : null
      let name = '';
      if (origName && origName.has(this.translate.currentLang)) {
        name = origName.get(this.translate.currentLang).replace(/\s/g, '').toLowerCase();
      }
      const action = data.action.replace('_', ' ').toLowerCase();
      const status = data.status.replace('_', ' ').toLowerCase();
      const requesterId = data.reqUserId.toString();

      return reqId.includes(filter) || name.includes(filter) || action.includes(filter)
        || status.includes(filter) || requesterId.includes(filter) || facilityId.includes(filter);
    });
  }

}
