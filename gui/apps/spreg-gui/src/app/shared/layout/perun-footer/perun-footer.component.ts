import {Component, OnInit} from '@angular/core';
import {Subscription} from "rxjs";
import {ConfigService} from "../../../core/services/config.service";


@Component({
  selector: 'app-perun-footer',
  template: "<div [innerHTML]=\"footerHTML\"></div>",
  //templateUrl: './perun-footer.component.html',
  styleUrls: ['./perun-footer.component.scss']
})
export class PerunFooterComponent implements OnInit {

  constructor(private configService: ConfigService) {}

  footerHTML : string;
  footerSubscription : Subscription;

  ngOnInit() {
    //TODO uncomment when implemented on backend

    // this.footerSubscription = this.configService.getFooter().subscribe(footer => {
    //   this.footerHTML = footer;
    // })
  }
}
