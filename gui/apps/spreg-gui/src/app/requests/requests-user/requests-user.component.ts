import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {RequestAction} from '../../core/models/enums/RequestAction';
import {RequestStatus} from '../../core/models/enums/RequestStatus';
import {RequestsService} from '../../core/services/requests.service';
import {Subscription} from 'rxjs';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {MatPaginator} from '@angular/material/paginator';
import {TranslateService} from '@ngx-translate/core';
import {RequestOverview} from '../../core/models/RequestOverview';

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
  ) { }

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
  dataSource: MatTableDataSource<RequestOverview> = new MatTableDataSource<RequestOverview>();

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

  setDataSource(): void {
    if (this.dataSource) {
      this.dataSource.data = this.requests;
      this.dataSource.sort = this.sort;
      this.dataSource.paginator = this.paginator;
      this.setSorting();
      this.setFiltering();
    }
  }

  doFilter(value: string): void {
    if (this.dataSource) {
      value = value ? value.trim().toLowerCase(): '';
      this.dataSource.filter = value;
    }
  }

  private setSorting(): void {
    if (!this.dataSource) {
      return;
    }
    this.dataSource.sortingDataAccessor = ((data, sortHeaderId) => {
      switch (sortHeaderId) {
        case 'id': {
          return data.id;
        }
        case 'serviceId': {
          return data.serviceId;
        }
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
          return RequestStatus[data.status];
        case 'action':
          return RequestAction[data.action];
        default:
          return data.id;
      }
    });
  }

  private setFiltering(): void {
    if (!this.dataSource) {
      return;
    }
    this.dataSource.filterPredicate = ((data: RequestOverview, filter: string) => {
      const id = data.id ? data.id.toString(): '';
      let name = '';
      if (data.serviceName && data.serviceName.has(this.translate.currentLang)) {
        name = data.serviceName.get(this.translate.currentLang).replace(/\s/g, '').toLowerCase();
      }
      const facilityId = data.serviceId ? data.serviceId.toString() : '';
      const action = RequestAction[data.action].replace('_', ' ').toLowerCase();
      const status = RequestStatus[data.status].replace('_', ' ').toLowerCase();
      const serviceIdentifier = data.serviceIdentifier.toLowerCase();
      const requesterId = data.requesterId.toString();

      const parts = filter.split(' ');
      for (let part of parts) {
        if (!(id.includes(filter) || name.includes(filter) || facilityId.includes(filter) || action.includes(filter)
          || serviceIdentifier.includes(filter) || status.includes(filter) || requesterId.includes(filter))) {
          return false;
        }
      }
      return true;
    });
  }

}
