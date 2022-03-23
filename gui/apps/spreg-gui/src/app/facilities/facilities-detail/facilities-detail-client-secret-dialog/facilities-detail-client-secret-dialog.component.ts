import {Component, Inject, OnInit} from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import {DialogData} from '../facilities-detail.component';

@Component({
  selector: 'app-facilities-detail-client-secret-dialog',
  templateUrl: './facilities-detail-client-secret-dialog.component.html',
  styleUrls: ['./facilities-detail-client-secret-dialog.component.scss']
})
export class FacilitiesDetailClientSecretDialogComponent implements OnInit {

  constructor(
    public dialogRef: MatDialogRef<FacilitiesDetailClientSecretDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData
  ) { }

  ngOnInit() { }

  onNoClick(): void {
    this.dialogRef.close();
  }

  onYesClick(): void {
    this.data.parent.loading = true;
    this.data.parent.regenerateClientSecret();
    this.dialogRef.close();
  }

}
