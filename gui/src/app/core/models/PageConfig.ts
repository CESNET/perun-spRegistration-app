export class PageConfig {
  constructor(item: any) {
    this.headerLabel = item.headerLabel;
    this.logoUrl = item.logoUrl;
    this.footerHtml = item.footerHtml;
    this.headerHtml = item.headerHtml;
    this.logoutUrl = item.logoutUrl;
    this.externalServices = item.externalServices;
    this.faviconUrl = item.faviconUrl;
  }

  headerLabel: string;
  logoUrl: string;
  footerHtml: string;
  headerHtml: string;
  logoutUrl: string;
  externalServices: boolean;
  faviconUrl: string;
}
