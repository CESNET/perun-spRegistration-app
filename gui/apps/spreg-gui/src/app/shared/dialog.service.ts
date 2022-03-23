import { Injectable } from '@angular/core';
import { MatDialog } from "@angular/material/dialog";
import {ErrorDialogComponent} from "./error-dialog/error-dialog.component";

@Injectable({
  providedIn: 'root'
})
export class DialogService {

  constructor(private dialog: MatDialog) { }

  openErrorDialog(data) : void {
    const dialogRef = this.dialog.open(ErrorDialogComponent, {
      width: '600px',
      data: data,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log(result);
    });
  }
}
