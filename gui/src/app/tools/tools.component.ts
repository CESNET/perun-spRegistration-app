import {Component, OnDestroy, OnInit} from '@angular/core';
import {ToolsService} from '../core/services/tools.service';
import {Subscription} from "rxjs";

@Component({
  selector: 'app-tools',
  templateUrl: './tools.component.html',
  styleUrls: ['./tools.component.scss']
})
export class ToolsComponent implements OnInit, OnDestroy {

  private toolsServiceEncryptSubscription: Subscription = null;
  private toolsServiceDecryptSubscription: Subscription = null;

  constructor(
    private toolsService: ToolsService
  ) { }

  loading = true;
  encryptOutput = '';
  decryptOutput = '';

  ngOnInit() {
    this.loading = false;
  }

  ngOnDestroy() {
    if (this.toolsServiceDecryptSubscription) {
      this.toolsServiceDecryptSubscription.unsubscribe();
    }
    if (this.toolsServiceEncryptSubscription) {
      this.toolsServiceEncryptSubscription.unsubscribe();
    }
  }

  encrypt(toEncrypt: string): void {
    this.toolsServiceEncryptSubscription = this.toolsService.encrypt(toEncrypt).subscribe(encryptedMap => {
      this.encryptOutput = encryptedMap['value'];
    });
  }

  decrypt(toDecrypt: string): void {
    this.toolsServiceDecryptSubscription = this.toolsService.decrypt(toDecrypt).subscribe(decryptedMap => {
      this.decryptOutput = decryptedMap['value'];
    });
  }

}
