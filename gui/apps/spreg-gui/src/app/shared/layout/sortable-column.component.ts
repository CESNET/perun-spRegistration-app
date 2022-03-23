import {Component, HostListener, Input, OnDestroy, OnInit} from '@angular/core';
import {Subscription} from "rxjs";
import {SortService} from "../../core/services/sort.service";

@Component({
  selector: '[sortable-column]',
  templateUrl: './sortable-column.component.html'
})
export class SortableColumnComponent implements OnInit, OnDestroy {

  constructor(private sortService: SortService) { }

  @Input('sortable-column')
  columnName: string;

  @Input('table-name')
  tableName: string = 'default';

  @Input('sort-direction')
  sortDirection: string = '';

  private columnSortedSubscription: Subscription;

  @HostListener('click')
  sort() {
    this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    this.sortService.columnSorted({
      tableName: this.tableName,
      sortColumn: this.columnName,
      sortDirection: this.sortDirection
    });
  }

  ngOnInit() {
    this.columnSortedSubscription = this.sortService.columnSorted$.subscribe(event => {
      if (this.tableName != event.tableName) {
        return;
      }
      if (this.columnName != event.sortColumn) {
        this.sortDirection = '';
      }
    });
  }

  ngOnDestroy() {
    this.columnSortedSubscription.unsubscribe();
  }

}
