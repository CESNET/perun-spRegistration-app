package cz.metacentrum.perun.spRegistration.service.impl;

import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.ProvidedService;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.managers.ProvidedServiceManager;
import cz.metacentrum.perun.spRegistration.service.StartupSyncService;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cz.metacentrum.perun.spRegistration.persistence.enums.ServiceEnvironment.PRODUCTION;
import static cz.metacentrum.perun.spRegistration.persistence.enums.ServiceEnvironment.TESTING;
import static cz.metacentrum.perun.spRegistration.persistence.enums.ServiceProtocol.OIDC;
import static cz.metacentrum.perun.spRegistration.persistence.enums.ServiceProtocol.SAML;

@Service("startupSyncService")
public class StartupSyncServiceImpl implements StartupSyncService {

    private static final Logger log = LoggerFactory.getLogger(StartupSyncServiceImpl.class);

    @NonNull private final ProvidedServiceManager providedServiceManager;
    @NonNull private final PerunAdapter perunAdapter;
    @NonNull private final AttributesProperties attributesProperties;

    @Autowired
    public StartupSyncServiceImpl(@NonNull ProvidedServiceManager providedServiceManager,
                                  @NonNull PerunAdapter perunAdapter,
                                  @NonNull ApplicationProperties applicationProperties,
                                  @NonNull AttributesProperties attributesProperties)
    {
        this.providedServiceManager = providedServiceManager;
        this.perunAdapter = perunAdapter;
        this.attributesProperties = attributesProperties;
        if (applicationProperties.isStartupSyncEnabled()) {
            createMissingProvidedServices();
        } else {
            log.info("Not performing startup sync, it is disable din the configuration");
        }
    }

    public void createMissingProvidedServices() {
        log.info("Syncing missing provided services");
        try {
            List<Facility> facilityList = perunAdapter.getFacilitiesByProxyIdentifier(
                    attributesProperties.getNames().getProxyIdentifier(),
                    attributesProperties.getValues().getProxyIdentifier());
            List<ProvidedService> providedServices = providedServiceManager.getAll();
            Set<Long> providedServicesFacIds = providedServices.stream()
                    .map(ProvidedService::getFacilityId)
                    .collect(Collectors.toSet());
            List<Facility> missing = new ArrayList<>();
            for (Facility f : facilityList) {
                if (!providedServicesFacIds.contains(f.getId())) {
                    missing.add(f);
                }
            }

            for (Facility missingFacility : missing) {
                try {
                    Map<String, PerunAttribute> attrs = perunAdapter.getFacilityAttributes(
                            missingFacility.getId(), attributesProperties.getAttrNames());

                    ProvidedService providedService = new ProvidedService();
                    providedService.setFacilityId(missingFacility.getId());
                    providedService.setName(attrs.get(attributesProperties.getNames().getServiceName()).valueAsMap());
                    providedService.setName(attrs.get(attributesProperties.getNames().getServiceDesc()).valueAsMap());
                    PerunAttribute clientId = attrs.getOrDefault(
                            attributesProperties.getNames().getOidcClientId(), null);
                    PerunAttribute entityId = attrs.getOrDefault(
                            attributesProperties.getNames().getEntityId(), null);
                    if (clientId != null && clientId.valueAsString() != null) {
                        providedService.setProtocol(OIDC);
                        providedService.setIdentifier(clientId.valueAsString());
                    } else if (entityId != null && entityId.valueAsString() != null) {
                        providedService.setProtocol(SAML);
                        providedService.setIdentifier(entityId.valueAsString());
                    }
                    PerunAttribute isTestSp = attrs.getOrDefault(attributesProperties.getNames().getIsTestSp(), null);
                    if (isTestSp != null && isTestSp.valueAsBoolean() != null) {
                        providedService.setEnvironment(isTestSp.valueAsBoolean() ? TESTING : PRODUCTION);
                    } else {
                        providedService.setEnvironment(TESTING);
                    }
                    providedServiceManager.create(providedService);
                } catch (Exception e) {
                    log.warn("Caught exception when syncing missing provided service for facility {}", missingFacility, e);
                }
            }
        } catch (Exception e) {
            log.warn("Caught exception when syncing missing provided services", e);
        }
        log.info("Syncing missing provided services finished");
    }

}

