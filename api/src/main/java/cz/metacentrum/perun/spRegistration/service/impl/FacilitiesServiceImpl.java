package cz.metacentrum.perun.spRegistration.service.impl;

import cz.metacentrum.perun.spRegistration.Utils;
import cz.metacentrum.perun.spRegistration.common.configs.AppConfig;
import cz.metacentrum.perun.spRegistration.common.configs.ApplicationBeans;
import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.common.configs.Config;
import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestManager;
import cz.metacentrum.perun.spRegistration.common.models.AttrInput;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.managers.ProvidedServiceManager;
import cz.metacentrum.perun.spRegistration.persistence.models.ProvidedService;
import cz.metacentrum.perun.spRegistration.service.FacilitiesService;
import cz.metacentrum.perun.spRegistration.service.ServiceUtils;
import cz.metacentrum.perun.spRegistration.service.UtilsService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class FacilitiesServiceImpl implements FacilitiesService {

    private final PerunAdapter perunAdapter;
    private final UtilsService utilsService;
    private final RequestManager requestManager;
    private final AttributesProperties attributesProperties;
    private final ApplicationBeans applicationBeans;
    private final ApplicationProperties applicationProperties;
    private final Config config;
    private final ProvidedServiceManager providedServiceManager;

    @Autowired
    public FacilitiesServiceImpl(PerunAdapter perunAdapter, UtilsService utilsService, RequestManager requestManager,
                                 Config config, ProvidedServiceManager providedServiceManager,
                                 AttributesProperties attributesProperties,
                                 ApplicationBeans applicationBeans, ApplicationProperties applicationProperties) {
        this.perunAdapter = perunAdapter;
        this.utilsService = utilsService;
        this.requestManager = requestManager;
        this.attributesProperties = attributesProperties;
        this.applicationBeans = applicationBeans;
        this.applicationProperties = applicationProperties;
        this.config = config;
        this.providedServiceManager = providedServiceManager;
    }

    @Override
    public Facility getFacility(Long facilityId, Long userId, boolean checkAdmin, boolean includeClientCredentials)
            throws UnauthorizedActionException, InternalErrorException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException, PerunUnknownException, PerunConnectionException {
        log.trace("getDetailedFacility(facilityId: {}, userId: {}, checkAdmin: {})", facilityId, userId, checkAdmin);

        if (Utils.checkParamsInvalid(facilityId, userId)) {
            log.error("Wrong parameters passed: (facilityId: {}, userId: {})", facilityId, userId);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        } else if (checkAdmin && !utilsService.isFacilityAdmin(facilityId, userId)) {
            log.error("User cannot view facility, user is not an admin");
            throw new UnauthorizedActionException("User cannot view facility, user is not an admin");
        }

        Facility facility = perunAdapter.getFacilityById(facilityId);
        if (facility == null) {
            log.error("Could not retrieve facility for id: {}", facilityId);
            throw new InternalErrorException("Could not retrieve facility for id: " + facilityId);
        }

        Long activeRequestId = requestManager.getActiveRequestIdByFacilityId(facilityId);
        facility.setActiveRequestId(activeRequestId);

        List<String> attrsToFetch = new ArrayList<>(applicationBeans.getAttributeDefinitionMap().keySet());
        Map<String, PerunAttribute> attrs = perunAdapter.getFacilityAttributes(facilityId, attrsToFetch);
        boolean isOidc = ServiceUtils.isOidcAttributes(attrs, attributesProperties.getEntityIdAttrName());
        List<String> keptAttrs = getAttrsToKeep(isOidc);
        List<PerunAttribute> filteredAttributes = ServiceUtils.filterFacilityAttrs(attrs, keptAttrs);
        Map<AttributeCategory, Map<String, PerunAttribute>> facilityAttributes = convertToStruct(filteredAttributes,
                applicationBeans);
        facility.setAttributes(facilityAttributes);

        Map<String, String> name = facility.getAttributes()
                .get(AttributeCategory.SERVICE)
                .get(attributesProperties.getServiceNameAttrName())
                .valueAsMap();

        Map<String, String> desc = facility.getAttributes()
                .get(AttributeCategory.SERVICE)
                .get(attributesProperties.getServiceDescAttrName())
                .valueAsMap();
        facility.setName(name);
        facility.setDescription(desc);

        if (isOidc) {
            PerunAttribute clientSecret = facility.getAttributes()
                    .get(AttributeCategory.PROTOCOL).get(attributesProperties.getOidcClientIdAttrName());
            String valEncrypted = clientSecret.valueAsString();
            String decrypted = ServiceUtils.decrypt(valEncrypted, applicationBeans.getSecretKeySpec());
            clientSecret.setValue(decrypted);
        }

        if (!includeClientCredentials) {
            facility.getAttributes().get(AttributeCategory.PROTOCOL)
                    .remove(attributesProperties.getOidcClientIdAttrName());
            facility.getAttributes().get(AttributeCategory.PROTOCOL)
                    .remove(attributesProperties.getOidcClientSecretAttrName());
        }

        boolean inTest = attrs.get(attributesProperties.getIsTestSpAttrName()).valueAsBoolean();
        facility.setTestEnv(inTest);

        Map<String, PerunAttribute> protocolAttrs = perunAdapter.getFacilityAttributes(facilityId,
                Arrays.asList(
                    attributesProperties.getIsOidcAttrName(),
                    attributesProperties.getIsSamlAttrName(),
                    attributesProperties.getMasterProxyIdentifierAttrName())
        );
        facility.setOidc(protocolAttrs.get(attributesProperties.getIsOidcAttrName()).valueAsBoolean());
        facility.setSaml(protocolAttrs.get(attributesProperties.getIsSamlAttrName()).valueAsBoolean());

        PerunAttribute proxyAttrs = protocolAttrs.get(attributesProperties.getMasterProxyIdentifierAttrName());
        boolean canBeEdited = attributesProperties.getMasterProxyIdentifierAttrValue()
                .equals(proxyAttrs.valueAsString());
        facility.setEditable(canBeEdited);

        log.trace("getDetailedFacility returns: {}", facility);
        return facility;
    }

    @Override
    public Facility getFacilityWithInputs(Long facilityId, Long userId)
            throws UnauthorizedActionException, InternalErrorException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException, PerunUnknownException, PerunConnectionException {
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
    public List<ProvidedService> getAllUserFacilities(Long userId) throws PerunUnknownException, PerunConnectionException {
        log.trace("getAllFacilitiesWhereUserIsAdmin({})", userId);

        if (Utils.checkParamsInvalid(userId)) {
            log.error("Wrong parameters passed: (userId: {})", userId);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        List<Facility> proxyFacilities = perunAdapter.getFacilitiesByProxyIdentifier(
                attributesProperties.getProxyIdentifierAttrName(),
                attributesProperties.getProxyIdentifierAttrValue()
        );
        Map<Long, Facility> proxyFacilitiesMap = ServiceUtils.transformListToMapFacilities(proxyFacilities);
        if (proxyFacilitiesMap == null || proxyFacilitiesMap.isEmpty()) {
            return new ArrayList<>();
        }

        List<Facility> userFacilities = perunAdapter.getFacilitiesWhereUserIsAdmin(userId);
        if (userFacilities == null || userFacilities.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> ids = userFacilities.stream().map(Facility::getId).collect(Collectors.toList());
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }

        List<Facility> testFacilities = perunAdapter.getFacilitiesByAttribute(
                attributesProperties.getIsTestSpAttrName(), "true");
        Map<Long, Facility> testFacilitiesMap = ServiceUtils.transformListToMapFacilities(testFacilities);
        if (testFacilitiesMap == null) {
            testFacilitiesMap = new HashMap<>();
        }

        List<Facility> oidcFacilities = perunAdapter.getFacilitiesByAttribute(
                attributesProperties.getIsOidcAttrName(), "true");
        Map<Long, Facility> oidcFacilitiesMap = ServiceUtils.transformListToMapFacilities(oidcFacilities);

        List<Facility> samlFacilities = perunAdapter.getFacilitiesByAttribute(
                attributesProperties.getIsSamlAttrName(), "true");
        Map<Long, Facility> samlFacilitiesMap = ServiceUtils.transformListToMapFacilities(samlFacilities);

        List<Facility> filteredFacilities = new ArrayList<>();

        for (Facility f : userFacilities) {
            if (proxyFacilitiesMap.containsKey(f.getId())) {
                filteredFacilities.add(f);

                f.setOidc(oidcFacilitiesMap.containsKey(f.getId()));
                f.setSaml(samlFacilitiesMap.containsKey(f.getId()));
                f.setTestEnv(testFacilitiesMap.containsKey(f.getId()));
            }
        }

        List<ProvidedService> services = providedServiceManager.getAllForFacilities(ids);
        log.trace("getAllFacilitiesWhereUserIsAdmin returns: {}", services);
        return services;
    }

    @Override
    public List<ProvidedService> getAllFacilities(Long userId)
        throws UnauthorizedActionException, PerunUnknownException, PerunConnectionException
    {
        log.trace("getAllFacilities({})", userId);

        if (Utils.checkParamsInvalid(userId)) {
            log.error("Wrong parameters passed: (userId: {})", userId);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        } else if (!applicationProperties.isAppAdmin(userId)) {
            log.error("User cannot list all facilities, user not an admin");
            throw new UnauthorizedActionException("User cannot list all facilities, user does not have role APP_ADMIN");
        }

        List<ProvidedService> services = providedServiceManager.getAll();
        List<Facility> proxyFacilities = perunAdapter.getFacilitiesByProxyIdentifier(
                attributesProperties.getProxyIdentifierAttrName(), attributesProperties.getProxyIdentifierAttrValue());
        Map<Long, Facility> proxyFacilitiesMap = ServiceUtils.transformListToMapFacilities(proxyFacilities);

        if (proxyFacilitiesMap == null || proxyFacilitiesMap.isEmpty()) {
            return new ArrayList<>();
        }

        List<Facility> testFacilities = perunAdapter.getFacilitiesByAttribute(
                attributesProperties.getIsTestSpAttrName(), "true");
        Map<Long, Facility> testFacilitiesMap = ServiceUtils.transformListToMapFacilities(testFacilities);

        List<Facility> oidcFacilities = perunAdapter.getFacilitiesByAttribute(
                attributesProperties.getIsOidcAttrName(), "true");
        Map<Long, Facility> oidcFacilitiesMap = ServiceUtils.transformListToMapFacilities(oidcFacilities);

        List<Facility> samlFacilities = perunAdapter.getFacilitiesByAttribute(
                attributesProperties.getIsSamlAttrName(), "true");
        Map<Long, Facility> samlFacilitiesMap = ServiceUtils.transformListToMapFacilities(samlFacilities);

        proxyFacilitiesMap.forEach((facId, val) -> {
            Facility f = proxyFacilitiesMap.get(facId);
            f.setTestEnv(testFacilitiesMap.containsKey(facId));
            f.setOidc(oidcFacilitiesMap.containsKey(facId));
            f.setSaml(samlFacilitiesMap.containsKey(facId));
        });

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
            keptAttrs.add(attributesProperties.getOidcClientIdAttrName());
            keptAttrs.add(attributesProperties.getOidcClientSecretAttrName());
        } else {
            keptAttrs.addAll(config.getSamlInputs()
                    .stream()
                    .map(AttrInput::getName)
                    .collect(Collectors.toList())
            );
        }

        return keptAttrs;
    }

    private Map<AttributeCategory, Map<String, PerunAttribute>> convertToStruct(List<PerunAttribute> filteredAttributes,
                                                                                ApplicationBeans appBeans)
    {
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
                AttributeCategory category = appBeans.getAttrCategory(attribute.getFullName());
                attribute.setInput(config.getInputMap().get(attribute.getFullName()));
                map.get(category).put(attribute.getFullName(), attribute);
            }
        }

        return map;
    }

}
