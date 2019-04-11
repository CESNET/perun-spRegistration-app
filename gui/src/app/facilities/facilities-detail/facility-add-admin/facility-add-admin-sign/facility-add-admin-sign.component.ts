import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {FacilitiesService} from "../../../../core/services/facilities.service";
import {MatSnackBar} from "@angular/material";
import {TranslateService} from "@ngx-translate/core";
import {Subscription} from "rxjs";

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
  facility: string;

  ngOnInit() {
    this.sub = this.route.queryParams.subscribe(params => {
      this.hash = params.code;
      this.facility = params.facilityName;
      if(this.hash == null || this.facility == null){
        this.router.navigate(['/notFound']);
      }
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
        this.translate.get('FACILITIES.ADD_ADMIN_SUCCES').subscribe(successMessage => {
          let snackBarRef = this.snackBar
            .open(successMessage, null, {duration: 5000});

          this.router.navigate(['/']);
        });
      } else {
        this.router.navigate(['/notFound']);
      }
    });
  }

}
