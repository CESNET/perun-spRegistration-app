import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {FacilitiesService} from "../core/services/facilities.service";
import {Subscription} from "rxjs";
import {Facility} from "../core/models/Facility";
import {MatSnackBar} from "@angular/material";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'app-document-sign',
  templateUrl: './document-sign.component.html',
  styleUrls: ['./document-sign.component.scss']
})
export class DocumentSignComponent implements OnInit, OnDestroy {

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
  private facility: Facility;

  ngOnInit() {
    this.sub = this.route.queryParams.subscribe(params => {
      if(params.code){
        this.hash = params.code;
        this.facilitiesService.getRequestDetailsWithHash(this.hash).subscribe(request =>{
          this.facilitiesService.getFacility(request.facilityId).subscribe(facility => {
            this.facility = facility;
            this.loading = false;
          }, error => {
            this.loading = false;
            console.log(error);
          });
        });
      }
      else{
        this.router.navigate(['/notFound']);
      }
    });
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  approveRequest(): void{
    this.facilitiesService.approveTransferToProduction(this.hash).subscribe(req => {
      this.translate.get('FACILITIES.DOCUMENT_SIGN_SUCCESS').subscribe(successMessage => {
        this.translate.get('FACILITIES.GO_TO_FACILITY_DETAIL').subscribe(goToFacilityMessage =>{
          let snackBarRef = this.snackBar
            .open(successMessage, goToFacilityMessage, {duration: 5000});

          snackBarRef.onAction().subscribe(() => {
            this.router.navigate(['/auth/facilities/detail/' + this.facility.id]);
          });

          this.router.navigate(['/auth']);
        });
      });
    });
  }

  rejectRequest(): void{
    this.facilitiesService.rejectTransferToProduction(this.hash).subscribe(req => {
      this.translate.get('FACILITIES.DOCUMENT_SIGN_SUCCESS').subscribe(successMessage => {
        this.translate.get('FACILITIES.GO_TO_FACILITY_DETAIL').subscribe(goToFacilityMessage =>{
          let snackBarRef = this.snackBar
            .open(successMessage, goToFacilityMessage, {duration: 5000});

          snackBarRef.onAction().subscribe(() => {
            this.router.navigate(['/auth/facilities/detail/' + this.facility.id]);
          });

          this.router.navigate(['/auth']);
        });
      });
    });
  }

}
