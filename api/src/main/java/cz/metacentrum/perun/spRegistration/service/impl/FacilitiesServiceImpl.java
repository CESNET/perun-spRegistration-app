package cz.metacentrum.perun.spRegistration.service.impl;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.metacentrum.perun.spRegistration.Utils;
import cz.metacentrum.perun.spRegistration.common.configs.AppBeansContainer;
import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.common.models.AttrInput;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.managers.ProvidedServiceManager;
import cz.metacentrum.perun.spRegistration.persistence.models.ProvidedService;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestManager;
import cz.metacentrum.perun.spRegistration.service.FacilitiesService;
import cz.metacentrum.perun.spRegistration.service.ServiceUtils;
import cz.metacentrum.perun.spRegistration.service.UtilsService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @NonNull private final PerunAdapter perunAdapter;
    @NonNull private final UtilsService utilsService;
    @NonNull private final RequestManager requestManager;
    @NonNull private final AttributesProperties attributesProperties;
    @NonNull private final AppBeansContainer appBeansContainer;
    @NonNull private final ApplicationProperties applicationProperties;
    @NonNull private final ProvidedServiceManager providedServiceManager;
    @NonNull private final Map<String, AttrInput> attrInputMap;
    @NonNull private final List<AttrInput> serviceInputs;
    @NonNull private final List<AttrInput> organizationInputs;
    @NonNull private final List<AttrInput> membershipInputs;
    @NonNull private final List<AttrInput> oidcInputs;
    @NonNull private final List<AttrInput> samlInputs;

    @Autowired
    public FacilitiesServiceImpl(PerunAdapter perunAdapter,
                                 UtilsService utilsService,
                                 RequestManager requestManager,
                                 AttributesProperties attributesProperties,
                                 AppBeansContainer appBeansContainer,
                                 ApplicationProperties applicationProperties,
                                 ProvidedServiceManager providedServiceManager,
                                 Map<String, AttrInput> attrInputMap,
                                 @Qualifier("serviceInputs") List<AttrInput> serviceInputs,
                                 @Qualifier("organizationInputs") List<AttrInput> organizationInputs,
                                 @Qualifier("membershipInputs") List<AttrInput> membershipInputs,
                                 @Qualifier("oidcInputs") List<AttrInput> oidcInputs,
                                 @Qualifier("samlInputs") List<AttrInput> samlInputs)
    {
        this.perunAdapter = perunAdapter;
        this.utilsService = utilsService;
        this.requestManager = requestManager;
        this.attributesProperties = attributesProperties;
        this.appBeansContainer = appBeansContainer;
        this.applicationProperties = applicationProperties;
        this.providedServiceManager = providedServiceManager;
        this.attrInputMap = attrInputMap;
        this.serviceInputs = serviceInputs;
        this.organizationInputs = organizationInputs;
        this.membershipInputs = membershipInputs;
        this.oidcInputs = oidcInputs;
        this.samlInputs = samlInputs;
    }

    @Override
    public Facility getFacility(@NonNull Long facilityId, @NonNull Long userId,
                                boolean checkAdmin, boolean includeClientCredentials)
            throws UnauthorizedActionException, InternalErrorException, BadPaddingException, InvalidKeyException,
            IllegalBlockSizeException, PerunUnknownException, PerunConnectionException
    {
        if (checkAdmin && !utilsService.isAdminForFacility(facilityId, userId)) {
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

        List<String> attrsToFetch = new ArrayList<>(appBeansContainer.getAllAttrNames());
        Map<String, PerunAttribute> attrs = perunAdapter.getFacilityAttributes(facilityId, attrsToFetch);
        boolean isOidc = ServiceUtils.isOidcAttributes(attrs, attributesProperties.getEntityIdAttrName());
        List<String> keptAttrs = this.getAttrsToKeep(isOidc);
        List<PerunAttribute> filteredAttributes = ServiceUtils.filterFacilityAttrs(attrs, keptAttrs);
        Map<AttributeCategory, Map<String, PerunAttribute>> facilityAttributes = this.convertToCategoryMap(filteredAttributes,
                appBeansContainer);
        facility.setAttributes(facilityAttributes);
        facility.setName(ServiceUtils.extractFacilityName(facility, attributesProperties));
        facility.setDescription(ServiceUtils.extractFacilityDescription(facility, attributesProperties));

        if (isOidc) {
            String clientSecretValue = this.extractClientSecretValueDecrypted(facility);
            facility.getAttributes()
                    .get(AttributeCategory.PROTOCOL)
                    .get(attributesProperties.getOidcClientIdAttrName())
                    .setValue(JsonNodeFactory.instance.textNode(clientSecretValue));
        }

        if (!includeClientCredentials) {
            this.clearOidcCredentials(facility);
        }

        facility.setTestEnv(attrs.get(attributesProperties.getIsTestSpAttrName()).valueAsBoolean());

        Map<String, PerunAttribute> additionalAttributes = perunAdapter.getFacilityAttributes(facility.getId(),
                Arrays.asList(
                        attributesProperties.getIsOidcAttrName(),
                        attributesProperties.getIsSamlAttrName(),
                        attributesProperties.getMasterProxyIdentifierAttrName()
                ));

        this.fillProtocolAttributes(facility, additionalAttributes);
        this.fillFacilityEditable(facility, additionalAttributes);

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
                                .forEach(attr -> attr.setInput(attrInputMap.get(attr.getFullName())))
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

    private void clearOidcCredentials(@NonNull Facility facility) {
        facility.getAttributes().get(AttributeCategory.PROTOCOL)
                .remove(attributesProperties.getOidcClientIdAttrName());
        facility.getAttributes().get(AttributeCategory.PROTOCOL)
                .remove(attributesProperties.getOidcClientSecretAttrName());
    }

    private List<String> getAttrsToKeep(boolean isOidc) {
        List<String> keptAttrs = new ArrayList<>();

        keptAttrs.addAll(serviceInputs.stream().map(AttrInput::getName).collect(Collectors.toList()));
        keptAttrs.addAll(organizationInputs.stream().map(AttrInput::getName).collect(Collectors.toList()));
        keptAttrs.addAll(membershipInputs.stream().map(AttrInput::getName).collect(Collectors.toList()));

        if (isOidc) {
            keptAttrs.addAll(oidcInputs.stream().map(AttrInput::getName).collect(Collectors.toList()));
            keptAttrs.add(attributesProperties.getOidcClientIdAttrName());
            keptAttrs.add(attributesProperties.getOidcClientSecretAttrName());
        } else {
            keptAttrs.addAll(samlInputs.stream().map(AttrInput::getName).collect(Collectors.toList()));
        }

        return keptAttrs;
    }

    private Map<AttributeCategory, Map<String, PerunAttribute>> convertToCategoryMap(List<PerunAttribute> filteredAttributes,
                                                                                     AppBeansContainer appBeans)
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
                attribute.setInput(attrInputMap.get(attribute.getFullName()));
                map.get(category).put(attribute.getFullName(), attribute);
            }
        }

        return map;
    }

    private String extractClientSecretValueDecrypted(@NonNull Facility facility)
            throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException
    {
        return this.extractClientSecretValue(facility, true);
    }

    private String extractClientSecretValue(@NonNull Facility facility, boolean decrypt)
            throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException
    {
        PerunAttribute clientSecret = facility.getAttributes()
                .get(AttributeCategory.PROTOCOL)
                .get(attributesProperties.getOidcClientIdAttrName());
        String valEncrypted = clientSecret.valueAsString();
        if (decrypt) {
            return ServiceUtils.decrypt(valEncrypted, appBeansContainer.getSecretKeySpec());
        } else {
            return valEncrypted;
        }
    }

    private void fillFacilityEditable(@NonNull Facility facility,
                                      @NonNull Map<String, PerunAttribute> additionalAttributes)
    {
        if (additionalAttributes != null
                && additionalAttributes.containsKey(attributesProperties.getMasterProxyIdentifierAttrName())) {
            PerunAttribute masterProxyIdentifierAttribute = additionalAttributes
                    .get(attributesProperties.getMasterProxyIdentifierAttrName());

            if (masterProxyIdentifierAttribute != null) {
                facility.setEditable(masterProxyIdentifierAttribute.valueAsBoolean());
            }
        }
    }

    private void fillProtocolAttributes(@NonNull Facility facility, @NonNull Map<String, PerunAttribute> attributeMap) {
        if (attributeMap.containsKey(attributesProperties.getIsOidcAttrName())) {
            PerunAttribute isOidc = attributeMap.get(attributesProperties.getIsOidcAttrName());
            if (isOidc != null) {
                facility.setOidc(attributeMap.get(attributesProperties.getIsOidcAttrName()).valueAsBoolean());
            }
        }

        if (attributeMap.containsKey(attributesProperties.getIsSamlAttrName())) {
            PerunAttribute isSaml = attributeMap.get(attributesProperties.getIsSamlAttrName());
            if (isSaml != null) {
                facility.setSaml(attributeMap.get(attributesProperties.getIsSamlAttrName()).valueAsBoolean());
            }
        }

    }

}
