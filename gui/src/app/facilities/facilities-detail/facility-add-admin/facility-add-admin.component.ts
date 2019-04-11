import { Component, OnInit } from '@angular/core';
import {Subscription} from "rxjs";
import {Facility} from "../../../core/models/Facility";
import {ActivatedRoute, Router} from "@angular/router";
import {FacilitiesService} from "../../../core/services/facilities.service";
import {MatChipInputEvent, MatSnackBar} from "@angular/material";
import {TranslateService} from "@ngx-translate/core";
import {ConfigService} from "../../../core/services/config.service";
import {COMMA, ENTER} from "@angular/cdk/keycodes";

@Component({
  selector: 'app-facility-add-admin',
  templateUrl: './facility-add-admin.component.html',
  styleUrls: ['./facility-add-admin.component.scss']
})
export class FacilityAddAdminComponent implements OnInit {

  private sub : Subscription;

  loading = true;

  facility: Facility;
  emails: string[];
  readonly separatorKeysCodes: number[] = [ENTER, COMMA];

  constructor(private route: ActivatedRoute,
              private facilitiesService: FacilitiesService,
              private snackBar: MatSnackBar,
              private translate: TranslateService,
              private configService: ConfigService,
              private router: Router) {}

  ngOnInit() {
    this.sub = this.route.params.subscribe(params => {
      this.facilitiesService.getFacility(params['id']).subscribe(facility => {
        this.facility = facility;
        this.emails = [];
        this.loading = false;
      });
    });
  }

  addAdmin() : void {
    if(this.emails.length == 0){
      this.translate.get('FACILITIES.EMAIL_ERROR_FILL').subscribe(result => {
        this.snackBar.open(result, null, {duration: 5000});
      });
      return;
    }
    this.facilitiesService.addAdmins(this.facility.id, this.emails).subscribe(response => {
      if(response){
        this.translate.get('FACILITIES.ADD_FACILITY_ADMINS_SUBMITED').subscribe(successMessage => {
          this.translate.get('FACILITIES.GO_TO_FACILITY_DETAIL').subscribe(goToFacilityMessage =>{
            let snackBarRef = this.snackBar
              .open(successMessage, goToFacilityMessage, {duration: 5000});

            snackBarRef.onAction().subscribe(() => {
              this.router.navigate(['/facilities/detail/' + this.facility.id]);
            });

            this.router.navigate(['/']);
          });
        });
      }
      else{
        this.router.navigate(['/**']);
      }

    });
  }

  add(event: MatChipInputEvent): void {
    const input = event.input;
    const value = event.value;

    if(value == ""){
      return;
    }

    let EMAIL_REGEXP = /[a-z0-9!#$%&'*+=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?/g;

    if (value.length <= 5 || !EMAIL_REGEXP.test(value)) {
      this.translate.get('FACILITIES.EMAIL_ERROR_CORRECT').subscribe(result => {
        this.snackBar.open(result, null, {duration: 5000});
      });
    } else{
        this.emails.push(value);
        if (input) {
          input.value = '';
        }
    }
  }

  remove(email: string): void {
    const index = this.emails.indexOf(email);
    if (index >= 0) {
      this.emails.splice(index, 1);
    }
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

}
