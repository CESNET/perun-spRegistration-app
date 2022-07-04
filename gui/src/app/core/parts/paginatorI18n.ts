import { MatPaginatorIntl } from '@angular/material/paginator';
import { TranslateParser, TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { Injectable, OnDestroy } from '@angular/core';

@Injectable()
export class PaginatorI18n extends MatPaginatorIntl implements OnDestroy {
  private rangeLabelIntl: string;
  private subscription: Subscription;

  constructor(
    private readonly translate: TranslateService,
    private readonly tParser: TranslateParser
  ) {
    super();
    this.getTranslations();
  }

  getTranslations() {
    this.subscription = this.translate
      .stream([
        'PAGINATOR.ITEMS_PER_PAGE_LABEL',
        'PAGINATOR.NEXT_PAGE_LABEL',
        'PAGINATOR.PREVIOUS_PAGE_LABEL',
        'PAGINATOR.FIRST_PAGE_LABEL',
        'PAGINATOR.LAST_PAGE_LABEL',
        'PAGINATOR.RANGE_PAGE_LABEL',
      ])
      .subscribe(translation => {
        this.itemsPerPageLabel = translation['PAGINATOR.ITEMS_PER_PAGE_LABEL'];
        this.nextPageLabel = translation['PAGINATOR.NEXT_PAGE_LABEL'];
        this.previousPageLabel = translation['PAGINATOR.PREVIOUS_PAGE_LABEL'];
        this.firstPageLabel = translation['PAGINATOR.FIRST_PAGE_LABEL'];
        this.lastPageLabel = translation['PAGINATOR.LAST_PAGE_LABEL'];
        this.rangeLabelIntl = translation['PAGINATOR.RANGE_PAGE_LABEL'];
        this.changes.next();
      });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  getRangeLabel = (page, pageSize, length) => {
    length = Math.max(length, 0);
    const startIndex = page * pageSize + 1;
    const endIndex =
      startIndex < length
        ? Math.min(startIndex + pageSize, length)
        : startIndex + pageSize;
    return this.tParser.interpolate(this.rangeLabelIntl, {
      startIndex,
      endIndex,
      length,
    });
  };
}
