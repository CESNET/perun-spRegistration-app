import {Component, Inject, OnInit} from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import {DialogData2} from '../facilities-detail.component';

@Component({
  selector: 'app-facilities-detail-remove-admin-dialog',
  templateUrl: './facilities-remove-admin-dialog.component.html',
  styleUrls: ['./facilities-remove-admin-dialog.component.scss']
})
export class FacilitiesRemoveAdminDialogComponent implements OnInit {

  constructor(
    public dialogRef: MatDialogRef<FacilitiesRemoveAdminDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData2
  ) { }

  ngOnInit() { }

  onNoClick(): void {
    this.dialogRef.close();
  }

  onYesClick(): void {
    this.data.parent.loading = true;
    this.data.parent.removeFacilityAdmin(this.data.userId);
    this.dialogRef.close();
  }

}
