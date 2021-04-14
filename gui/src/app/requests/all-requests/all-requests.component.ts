import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import { RequestsService } from '../../core/services/requests.service';
import { Request } from '../../core/models/Request';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Subscription } from 'rxjs';
import { MatPaginator } from '@angular/material/paginator';
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'app-all-requests',
  templateUrl: './all-requests.component.html',
  styleUrls: ['./all-requests.component.scss']
})
export class AllRequestsComponent implements OnInit, OnDestroy {

  constructor(
    private requestsService: RequestsService,
    private translate: TranslateService)
  {
    this.requests = [];
    this.dataSource = new MatTableDataSource(this.requests);
    this.setFiltering();
    this.setSorting();
  }

  @ViewChild(MatPaginator, {static: false}) set matPaginator(mp: MatPaginator) {
    this.paginator = mp;
    this.setDataSource();
  }

  @ViewChild(MatSort, {static: false}) set matSort(ms: MatSort) {
    this.sort = ms;
    this.setDataSource();
  }

  private paginator: MatPaginator = undefined;
  private sort: MatSort = undefined;
  private requestsSubscription: Subscription;

  requests: Request[];

  loading = true;
  displayedColumns: string[] = ['id', 'serviceName', 'reqUser', 'status', 'action'];
  dataSource: MatTableDataSource<Request> = new MatTableDataSource<Request>([]);

  setDataSource() {
    this.dataSource = new MatTableDataSource<Request>(this.requests);
    if (!!this.sort) {
      this.dataSource.sort = this.sort;
    }
    if (!!this.paginator) {
      this.dataSource.paginator = this.paginator;
    }
    this.setFiltering();
    this.setSorting();
  }

  ngOnInit() {
    this.requestsSubscription = this.requestsService.getAllRequests().subscribe(requests => {
      this.requests = requests.map(r => new Request(r));
      this.setDataSource();
      this.loading = false;
    }, _ => {
      this.loading = false;
    });
  }

  ngOnDestroy(): void {
    this.requestsSubscription.unsubscribe();
  }

  doFilter(value: string) {
    this.dataSource.filter = value.trim().toLowerCase();
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
