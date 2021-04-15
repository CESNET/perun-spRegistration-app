import {Component, Inject, OnInit} from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import {DialogData} from "../requests-detail.component";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'app-request-detail-dialog',
  templateUrl: './requests-detail-dialog.component.html',
  styleUrls: ['./requests-detail-dialog.component.scss']
})
export class RequestsDetailDialogComponent implements OnInit {

  constructor(
    public dialogRef: MatDialogRef<RequestsDetailDialogComponent>,
    private translate: TranslateService,
    @Inject(MAT_DIALOG_DATA) public data: DialogData
  ) { }

  promptText: string;

  ngOnInit() {
    if (this.data.isApprove) {
      this.translate.get('REQUESTS.ARE_YOU_SURE_APPROVE').subscribe(value => this.promptText = value);
    } else if (this.data.isSetWFC) {
      this.translate.get('REQUESTS.ARE_YOU_SURE_SET_WFC').subscribe(value => this.promptText = value);
    } else if (this.data.isCancel) {
      this.translate.get('REQUESTS.ARE_YOU_SURE_CANCEL').subscribe(value => this.promptText = value);
    } else {
      this.translate.get('REQUESTS.ARE_YOU_SURE_REJECT').subscribe(value => this.promptText = value);
    }
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  onYesClick(): void {
    this.data.parent.onLoading();
    if (this.data.isApprove) {
      this.data.parent.approve();
    } else if (this.data.isSetWFC) {
      this.data.parent.requestChanges();
    } else if (this.data.isCancel) {
      this.data.parent.cancel();
    } else {
      this.data.parent.reject();
    }

    this.dialogRef.close();
  }

}
