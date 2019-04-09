import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {FacilitiesService} from "../../../../core/services/facilities.service";
import {MatSnackBar} from "@angular/material";
import {TranslateService} from "@ngx-translate/core";
import {Subscription} from "rxjs";
import {Facility} from "../../../../core/models/Facility";

@Component({
  selector: 'app-facility-add-admin-sign',
  templateUrl: './facility-add-admin-sign.component.html',
  styleUrls: ['./facility-add-admin-sign.component.scss']
})
export class FacilityAddAdminSignComponent implements OnInit {

  constructor(
    private route: ActivatedRoute,
    private facilitiesService: FacilitiesService,
    private router: Router,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
  ) {}

  private sub : Subscription;
  loading = true;

  private hash : string;
  //private facility: Facility;

  ngOnInit() {
    this.sub = this.route.queryParams.subscribe(params => {
      this.hash = params.hash;
      this.loading = false;
    }, error => {
      this.loading = false;
      console.log(error);
    });
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  addAdmin(): void{
    this.facilitiesService.addAdminConfirm(this.hash).subscribe(response => {
      if (response) {
        this.translate.get('FACILITIES.ADD_ADMIN').subscribe(successMessage => {
          let snackBarRef = this.snackBar
            .open(successMessage, null, {duration: 5000});

          this.router.navigate(['/']);
        });
      } else {
        this.router.navigate(['/**']);
      }
    });
  }

}
