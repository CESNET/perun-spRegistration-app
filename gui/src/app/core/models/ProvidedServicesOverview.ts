import {ProvidedServiceOverview} from "./ProvidedServiceOverview";

export class ProvidedServicesOverview {
  constructor(item: any) {
    if (!item) {
      return;
    }

    this.samlServicesCount = item.samlServicesCount;
    this.oidcServicesCount = item.oidcServicesCount;

    this.productionServices = item.services['PRODUCTION'].map(s => new ProvidedServiceOverview(s));

    if (item.services['STAGING']) {
      this.displayableStagingServices = true;
      this.stagingServices = item.services['STAGING'].map(s => new ProvidedServiceOverview(s));
    }
    if (item.services['TESTING']) {
      this.displayableTestingServices = true;
      this.testingServices = item.services['TESTING'].map(s => new ProvidedServiceOverview(s));
    }
  }

  samlServicesCount: Map<string, bigint>;
  oidcServicesCount: Map<string, bigint>;

  testingServices: ProvidedServiceOverview[] = [];
  productionServices: ProvidedServiceOverview[] = [];
  stagingServices: ProvidedServiceOverview[] = [];

  displayableStagingServices = false;
  displayableTestingServices = false;

}
