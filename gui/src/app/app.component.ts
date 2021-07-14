import {Component, OnInit} from '@angular/core';
import {MatDrawerMode} from '@angular/material/sidenav';
import { TranslateService } from '@ngx-translate/core';
import { Router } from '@angular/router';
import { HostListener } from '@angular/core';
import { UsersService } from './core/services/users.service';
import { ConfigService } from './core/services/config.service';
import { PageConfig } from './core/models/PageConfig';
import { User } from './core/models/User';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  constructor(
    private configService: ConfigService,
    private userService: UsersService,
    private translate: TranslateService,
    private router: Router
  ) {
    this.onResize();
    this.currentUrl = this.router.url;
    this.loading = true;

    this.configService.getLanguages().subscribe(langs => {
      this.langs = langs;
      this.translate.addLangs(langs);
      this.translate.setDefaultLang('en');

      const browserLang = this.translate.getBrowserLang();
      if (this.langs.includes(browserLang)) {
        this.translate.use(browserLang);
      } else {
        this.translate.use(this.translate.getDefaultLang());
      }

      const storedLang = localStorage.getItem('lang');
      if (storedLang && this.translate.langs.indexOf(storedLang) >= 0) {
        this.translate.use(storedLang);
      } else {
        localStorage.setItem('lang', this.translate.currentLang);
      }
    });

    router.events.subscribe(_ => {
      this.currentUrl = this.router.url;
      if (this.currentUrl.includes('auth') && !this.hasUser()) {
        this.setAndGetUser();
      }
    });

    this.setAndGetUser();
  }

  static pageConfig: PageConfig;
  static user: User;

  sidenavOpen = true;
  loading = true;
  minWidth = 768;
  sidenavMode: MatDrawerMode = 'side';
  currentUrl = '';
  logoUrl = '';
  appTitle = '';
  langs: string[];

  lastWindowWidth: number;

  public static isApplicationAdmin(): boolean {
    if (this.user) {
      return this.user.isAppAdmin;
    }

    return false;
  }

  public static getUser(): User {
    if (this.user === undefined || this.user === null) {
      return null;
    }

    return this.user;
  }

  public static setUser(user: User): void {
    this.user = user;
  }

  public static hasUser() {
    return (AppComponent.getUser() !== null && AppComponent.getUser() !== undefined);
  }

  @HostListener('window:resize', ['$event'])
  onResize(event?) {
    if (this.sidenavOpen && this.lastWindowWidth > window.innerWidth &&
      window.innerWidth < this.minWidth) {
      this.sidenavOpen = false;
    }

    this.sidenavMode = window.innerWidth > this.minWidth ? 'side' : 'over';
    this.lastWindowWidth = window.innerWidth;
  }

  public logout(): void {
    this.userService.unsetUser().subscribe(resp => {
      window.location.href = AppComponent.pageConfig.logoutUrl;
    });
  }

  public changeLanguage(lang: string) {
    this.translate.use(lang);
    localStorage.setItem('lang', lang);
  }

  public toggleSideBar() {
    this.sidenavOpen = !this.sidenavOpen;
  }

  public hasUser(): boolean {
    return !!AppComponent.getUser();
  }

  public getUser(): User {
    return AppComponent.user;
  }

  private setAndGetUser() {
    this.userService.getUser().subscribe(user => {
      if (user !== undefined && user !== null) {
        AppComponent.setUser(new User(user));
      } else {
        this.goOnLogin();
      }
    });
  }

  private goOnLogin() {
    this.router.navigate(['/']);
    AppComponent.setUser(null);
  }

  ngOnInit(): void {
    this.configService.getPageConfig().subscribe(pageConfig => {
      if (pageConfig !== null && pageConfig !== undefined) {
        AppComponent.pageConfig = new PageConfig(pageConfig);
        this.appTitle = pageConfig.headerLabel;
        this.logoUrl = pageConfig.logoUrl;
      }
      this.loading = false;
    });
    this.setAndGetUser();
  }

}
