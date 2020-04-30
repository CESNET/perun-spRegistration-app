package cz.metacentrum.perun.spRegistration.service.impl;

import cz.metacentrum.perun.spRegistration.Utils;
import cz.metacentrum.perun.spRegistration.common.configs.AppConfig;
import cz.metacentrum.perun.spRegistration.common.configs.Config;
import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.exceptions.ConnectorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.AttrInput;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.connectors.PerunConnector;
import cz.metacentrum.perun.spRegistration.persistence.managers.ProvidedServiceManager;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestManager;
import cz.metacentrum.perun.spRegistration.persistence.models.ProvidedService;
import cz.metacentrum.perun.spRegistration.service.FacilitiesService;
import cz.metacentrum.perun.spRegistration.service.ServiceUtils;
import cz.metacentrum.perun.spRegistration.service.UtilsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("facilitiesService")
public class FacilitiesServiceImpl implements FacilitiesService {

    private static final Logger log = LoggerFactory.getLogger(FacilitiesServiceImpl.class);

    private final PerunConnector perunConnector;
    private final UtilsService utilsService;
    private final RequestManager requestManager;
    private final AppConfig appConfig;
    private final Config config;
    private final ProvidedServiceManager providedServiceManager;

    @Autowired
    public FacilitiesServiceImpl(PerunConnector perunConnector, UtilsService utilsService, RequestManager requestManager,
                                 AppConfig appConfig, Config config, ProvidedServiceManager providedServiceManager) {
        this.perunConnector = perunConnector;
        this.utilsService = utilsService;
        this.requestManager = requestManager;
        this.appConfig = appConfig;
        this.config = config;
        this.providedServiceManager = providedServiceManager;
    }

    @Override
    public Facility getFacility(Long facilityId, Long userId, boolean checkAdmin, boolean includeClientCredentials)
            throws UnauthorizedActionException, ConnectorException, InternalErrorException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
        log.trace("getDetailedFacility(facilityId: {}, userId: {}, checkAdmin: {})", facilityId, userId, checkAdmin);

        if (Utils.checkParamsInvalid(facilityId, userId)) {
            log.error("Wrong parameters passed: (facilityId: {}, userId: {})", facilityId, userId);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        } else if (checkAdmin && !utilsService.isFacilityAdmin(facilityId, userId)) {
            log.error("User cannot view facility, user is not an admin");
            throw new UnauthorizedActionException("User cannot view facility, user is not an admin");
        }

        Facility facility = perunConnector.getFacilityById(facilityId);
        if (facility == null) {
            log.error("Could not retrieve facility for id: {}", facilityId);
            throw new InternalErrorException("Could not retrieve facility for id: " + facilityId);
        }

        Long activeRequestId = requestManager.getActiveRequestIdByFacilityId(facilityId);
        facility.setActiveRequestId(activeRequestId);

        List<String> attrsToFetch = new ArrayList<>(appConfig.getPerunAttributeDefinitionsMap().keySet());
        Map<String, PerunAttribute> attrs = perunConnector.getFacilityAttributes(facilityId, attrsToFetch);
        boolean isOidc = ServiceUtils.isOidcAttributes(attrs, appConfig.getEntityIdAttribute());
        List<String> keptAttrs = getAttrsToKeep(isOidc);
        List<PerunAttribute> filteredAttributes = ServiceUtils.filterFacilityAttrs(attrs, keptAttrs);
        Map<AttributeCategory, Map<String, PerunAttribute>> facilityAttributes = convertToStruct(filteredAttributes, appConfig);
        facility.setAttributes(facilityAttributes);

        Map<String, String> name = facility.getAttributes()
                .get(AttributeCategory.SERVICE)
                .get(appConfig.getServiceNameAttributeName())
                .valueAsMap();

        Map<String, String> desc = facility.getAttributes()
                .get(AttributeCategory.SERVICE)
                .get(appConfig.getServiceDescAttributeName())
                .valueAsMap();
        facility.setName(name);
        facility.setDescription(desc);

        if (isOidc) {
            PerunAttribute clientSecret = facility.getAttributes()
                    .get(AttributeCategory.PROTOCOL).get(appConfig.getClientSecretAttribute());
            String valEncrypted = clientSecret.valueAsString();
            String decrypted = ServiceUtils.decrypt(valEncrypted, appConfig.getSecret());
            clientSecret.setValue(decrypted);
        }

        if (!includeClientCredentials) {
            facility.getAttributes().get(AttributeCategory.PROTOCOL).remove(appConfig.getClientIdAttribute());
            facility.getAttributes().get(AttributeCategory.PROTOCOL).remove(appConfig.getClientSecretAttribute());
        }

        boolean inTest = attrs.get(appConfig.getIsTestSpAttribute()).valueAsBoolean();
        facility.setTestEnv(inTest);

        Map<String, PerunAttribute> protocolAttrs = perunConnector.getFacilityAttributes(facilityId, Arrays.asList(
                appConfig.getIsOidcAttributeName(), appConfig.getIsSamlAttributeName(), appConfig.getMasterProxyIdentifierAttribute()));
        facility.setOidc(protocolAttrs.get(appConfig.getIsOidcAttributeName()).valueAsBoolean());
        facility.setSaml(protocolAttrs.get(appConfig.getIsSamlAttributeName()).valueAsBoolean());

        PerunAttribute proxyAttrs = protocolAttrs.get(appConfig.getMasterProxyIdentifierAttribute());
        boolean canBeEdited = appConfig.getMasterProxyIdentifierAttributeValue().equals(proxyAttrs.valueAsString());
        facility.setEditable(canBeEdited);

        log.trace("getDetailedFacility returns: {}", facility);
        return facility;
    }

    @Override
    public Facility getFacilityWithInputs(Long facilityId, Long userId)
            throws UnauthorizedActionException, ConnectorException, InternalErrorException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
        log.trace("getDetailedFacilityWithInputs(facilityId: {}, userId: {})", facilityId, userId);

        if (Utils.checkParamsInvalid(facilityId, userId)) {
            log.error("Wrong parameters passed: (facilityId: {}, userId: {})", facilityId, userId);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        Facility facility = getFacility(facilityId, userId, true, false);
        if (facility == null || facility.getAttributes() == null) {
            log.error("Could not fetch facility for id: {}", facilityId);
            throw new InternalErrorException("Could not fetch facility for id: " + facilityId);
        }

        facility.getAttributes()
                .values()
                .forEach(
                        attrsInCategory -> attrsInCategory.values()
                                .forEach(attr -> attr.setInput(config.getInputMap().get(attr.getFullName())))
                );

        log.trace("getDetailedFacilityWithInputs() returns: {}", facility);
        return facility;
    }

    @Override
    public List<ProvidedService> getAllUserFacilities(Long userId) throws ConnectorException {
        log.trace("getAllFacilitiesWhereUserIsAdmin({})", userId);

        if (Utils.checkParamsInvalid(userId)) {
            log.error("Wrong parameters passed: (userId: {})", userId);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        List<Facility> userFacilities = perunConnector.getFacilitiesWhereUserIsAdmin(userId);
        if (userFacilities == null || userFacilities.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> ids = userFacilities.stream().map(Facility::getId).collect(Collectors.toList());
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }

        List<ProvidedService> services = providedServiceManager.getAllForFacilities(ids);
        log.trace("getAllFacilitiesWhereUserIsAdmin returns: {}", services);
        return services;
    }

    @Override
    public List<ProvidedService> getAllFacilities(Long userId) throws UnauthorizedActionException, ConnectorException {
        log.trace("getAllFacilities({})", userId);

        if (Utils.checkParamsInvalid(userId)) {
            log.error("Wrong parameters passed: (userId: {})", userId);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        } else if (! appConfig.isAppAdmin(userId)) {
            log.error("User cannot list all facilities, user not an admin");
            throw new UnauthorizedActionException("User cannot list all facilities, user does not have role APP_ADMIN");
        }

        List<ProvidedService> services = providedServiceManager.getAll();

        log.trace("getAllFacilities returns: {}", services);
        return services;
    }

    private List<String> getAttrsToKeep(boolean isOidc) {
        List<String> keptAttrs = new ArrayList<>();

        keptAttrs.addAll(config.getServiceInputs().stream()
                .map(AttrInput::getName)
                .collect(Collectors.toList()));

        keptAttrs.addAll(config.getOrganizationInputs().stream()
                .map(AttrInput::getName)
                .collect(Collectors.toList()));

        keptAttrs.addAll(config.getMembershipInputs().stream()
                .map(AttrInput::getName)
                .collect(Collectors.toList()));

        if (isOidc) {
            keptAttrs.addAll(config.getOidcInputs()
                    .stream()
                    .map(AttrInput::getName)
                    .collect(Collectors.toList())
            );
            keptAttrs.add(appConfig.getClientIdAttribute());
            keptAttrs.add(appConfig.getClientSecretAttribute());
        } else {
            keptAttrs.addAll(config.getSamlInputs()
                    .stream()
                    .map(AttrInput::getName)
                    .collect(Collectors.toList())
            );
        }

        return keptAttrs;
    }

    private Map<AttributeCategory, Map<String, PerunAttribute>> convertToStruct(List<PerunAttribute> filteredAttributes, AppConfig appConfig) {
        if (filteredAttributes == null) {
            return null;
        }

        Map<AttributeCategory, Map<String, PerunAttribute>> map = new HashMap<>();
        map.put(AttributeCategory.SERVICE, new HashMap<>());
        map.put(AttributeCategory.ORGANIZATION, new HashMap<>());
        map.put(AttributeCategory.PROTOCOL, new HashMap<>());
        map.put(AttributeCategory.ACCESS_CONTROL, new HashMap<>());

        if (!filteredAttributes.isEmpty()) {
            for (PerunAttribute attribute : filteredAttributes) {
                AttributeCategory category = appConfig.getAttrCategory(attribute.getFullName());
                attribute.setInput(config.getInputMap().get(attribute.getFullName()));
                map.get(category).put(attribute.getFullName(), attribute);
            }
        }

        return map;
    }

}
