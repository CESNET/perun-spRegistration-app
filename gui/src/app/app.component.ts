import {Component, OnInit} from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import {NavigationEnd, Router} from "@angular/router";
import { HostListener } from "@angular/core";
import {UsersService} from "./core/services/users.service";
import {ConfigService} from "./core/services/config.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  sideBarOpened = false;

  lastScreenWidth: number;

  minWidth = 768;
  sidebarMode = 'side';
  currentUrl: string;

  userLoggedIn = false;

  isAdmin : boolean;

  constructor(
    private userService: UsersService,
    private translate: TranslateService,
    private router: Router,
    private configService: ConfigService
  ) {
    this.getScreenSize();

    translate.setDefaultLang('en');

    // TODO remove on production
    translate.use('en');
    router.events.subscribe((_: NavigationEnd) => {
      this.currentUrl = this.router.url;
    });

    //configService.isUserAdmin().subscribe(bool => this.isAdmin = bool);
  }

  @HostListener('window:resize', ['$event'])
  getScreenSize(event?) {

    if (window.innerWidth > this.minWidth) {
      this.sideBarOpened = true;
      this.sidebarMode = 'side';
    } else if (this.lastScreenWidth > this.minWidth) {
      this.sideBarOpened = false;
    }

    if (window.innerWidth <= this.minWidth) {
      this.sidebarMode = 'over';
    }

    this.lastScreenWidth = window.innerWidth;
  }

  closeSideBar() {
      if (this.sidebarMode == 'over') {
          this.sideBarOpened = false;
      }
  }

  ngOnInit(): void {
    if (!this.userLoggedIn) {
      this.userService.login().subscribe(() => {
        this.userLoggedIn = true;
      });
    }
    this.configService.isUserAdmin().subscribe(bool => this.isAdmin = bool);
  }
}
