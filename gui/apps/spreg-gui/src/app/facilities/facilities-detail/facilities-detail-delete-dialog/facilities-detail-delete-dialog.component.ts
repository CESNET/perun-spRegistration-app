import {Component, Inject, OnInit} from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import {DialogData} from '../facilities-detail.component';

@Component({
  selector: 'app-facilities-detail-delete-dialog',
  templateUrl: './facilities-detail-delete-dialog.component.html',
  styleUrls: ['./facilities-detail-delete-dialog.component.scss']
})
export class FacilitiesDetailDeleteDialogComponent implements OnInit {

  constructor(
    public dialogRef: MatDialogRef<FacilitiesDetailDeleteDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData
  ) { }

  ngOnInit() { }

  onNoClick(): void {
    this.dialogRef.close();
  }

  onYesClick(): void {
    this.data.parent.loading = true;
    this.data.parent.deleteFacility();
    this.dialogRef.close();
  }

}
