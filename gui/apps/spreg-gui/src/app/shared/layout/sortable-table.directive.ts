import { Directive, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { SortService } from "../../core/services/sort.service";
import { Subscription } from "rxjs";

@Directive({
  selector: '[app-sortable-table]'
})
export class SortableTableDirective implements OnInit, OnDestroy {

  constructor(private sortService: SortService) { }

  @Output()
  sorted = new EventEmitter();

  private columnSortedSubscription: Subscription;

  ngOnInit() {
    this.columnSortedSubscription = this.sortService.columnSorted$.subscribe(event => {
      this.sorted.emit(event);
    });
  }

  ngOnDestroy() {
    this.columnSortedSubscription.unsubscribe();
  }
}
