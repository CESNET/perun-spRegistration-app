package cz.metacentrum.perun.spRegistration.common.models;

import cz.metacentrum.perun.spRegistration.persistence.enums.ServiceEnvironment;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class ProvidedServicesOverview {

    private Map<ServiceEnvironment, Integer> samlServicesCount;
    private Map<ServiceEnvironment, Integer> oidcServicesCount;

    private Map<ServiceEnvironment, List<ProvidedServiceOverview>> services;

}
