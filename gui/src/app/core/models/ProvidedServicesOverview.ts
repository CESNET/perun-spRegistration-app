import {ProvidedServiceOverview} from "./ProvidedServiceOverview";

export class ProvidedServicesOverview {
  samlServicesCount: Map<string, number>;
  oidcServicesCount: Map<string, number>;

  productionServices: ProvidedServiceOverview[] = [];
  stagingServices: ProvidedServiceOverview[] = [];
  testingServices: ProvidedServiceOverview[] = [];

  displayableProductionServices = false;
  displayableStagingServices = false;
  displayableTestingServices = false;

  constructor(item: any) {
    this.samlServicesCount = new Map<string, number>();
    this.oidcServicesCount = new Map<string, number>();
    this.productionServices = [];
    this.stagingServices = [];
    this.testingServices = [];
    this.displayableProductionServices = false;
    this.displayableStagingServices = false;
    this.displayableTestingServices = false;

    if (!item) {
      return;
    }

    if (item['samlServicesCount']) {
      for (const k of Object.keys(item.samlServicesCount)) {
        this.samlServicesCount.set(k, item.samlServicesCount[k]);
      }
    }

    if (item['oidcServicesCount']) {
      for (const k of Object.keys(item.oidcServicesCount)) {
        this.oidcServicesCount.set(k, item.oidcServicesCount[k]);
      }
    }

    if (item['services'] && item.services['PRODUCTION']) {
      this.displayableProductionServices = true;
      item.services['PRODUCTION'].map(s => this.productionServices.push(new ProvidedServiceOverview(s)));
    }

    if (item['services'] && item.services['STAGING']) {
      this.displayableStagingServices = true;
      item.services['STAGING'].map(s => this.stagingServices.push(new ProvidedServiceOverview(s)));
    }

    if (item['services'] && item.services['TESTING']) {
      this.displayableTestingServices = true;
      item.services['TESTING'].map(s => this.testingServices.push(new ProvidedServiceOverview(s)));
    }
  }

}
