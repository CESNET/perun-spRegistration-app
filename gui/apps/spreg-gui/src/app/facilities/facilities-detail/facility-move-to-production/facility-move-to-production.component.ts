import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {FacilitiesService} from '../../../core/services/facilities.service';
import {Subscription} from 'rxjs';
import {Facility} from '../../../core/models/Facility';
import { MatSnackBar } from '@angular/material/snack-bar';
import {TranslateService} from '@ngx-translate/core';
import {ConfigService} from '../../../core/services/config.service';

@Component({
  selector: 'app-facility-move-to-production',
  templateUrl: './facility-move-to-production.component.html',
  styleUrls: ['./facility-move-to-production.component.scss']
})
export class FacilityMoveToProductionComponent implements OnInit, OnDestroy {

  constructor(
    private route: ActivatedRoute,
    private facilitiesService: FacilitiesService,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
    private configService: ConfigService,
    private router: Router
  ) { }

  private sub: Subscription;

  authorities: string[];
  loading = true;
  facility: Facility;
  emails: string[] = [];
  selectEmailsEnabled = false;
  specifyFromList = false;
  successActionText: string;

  ngOnInit() {
    this.sub = this.route.params.subscribe(params => {
      this.facilitiesService.getFacility(params['id']).subscribe(facility => {
        this.configService.isAuthoritiesEnabled().subscribe(response => {
          this.selectEmailsEnabled = response;
          this.specifyFromList = response;
          this.facility = new Facility(facility);
          this.emails = [];
          this.loading = false;
        });
      });
      this.configService.getProdTransferEntries().subscribe(entries => {
        this.authorities = entries;
      });
    });
    this.translate.get('REQUESTS.SUCCESSFULLY_SUBMITTED')
      .subscribe(value => this.successActionText = value);
  }

  moveToProduction(): void {
    this.loading = true;
    if (!this.selectEmailsEnabled) {
      this.emails = [];
    }

    if (this.specifyFromList && this.emails.length === 0) {
      this.translate.get('FACILITIES.EMAIL_ERROR_FILL').subscribe(result => {
        this.snackBar.open(result, null, {duration: 5000});
      });
      this.loading = false;
      return;
    }

    this.facilitiesService.createRequest(this.facility.id, this.emails).subscribe(reqid => {
      this.loading = false;
      this.snackBar.open(this.successActionText, null, {duration: 5000});
      this.router.navigate(['/auth/requests/detail/' + reqid]);
    });
  }

  checkboxChanged(email: string): void {
    const index = this.emails.indexOf(email);
    if (index >= 0) {
      this.emails.splice(index, 1);
    } else {
      this.emails.push(email);
    }
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

}
