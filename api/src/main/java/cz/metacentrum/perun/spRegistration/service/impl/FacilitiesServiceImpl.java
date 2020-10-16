package cz.metacentrum.perun.spRegistration.service.impl;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.metacentrum.perun.spRegistration.common.configs.AppBeansContainer;
import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.InputsContainer;
import cz.metacentrum.perun.spRegistration.common.models.PerunEntity;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.common.models.AttrInput;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.managers.ProvidedServiceManager;
import cz.metacentrum.perun.spRegistration.common.models.ProvidedService;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestManager;
import cz.metacentrum.perun.spRegistration.service.FacilitiesService;
import cz.metacentrum.perun.spRegistration.service.ServiceUtils;
import cz.metacentrum.perun.spRegistration.service.UtilsService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    @NonNull private final InputsContainer inputsContainer;

    @Autowired
    public FacilitiesServiceImpl(@NonNull PerunAdapter perunAdapter,
                                 @NonNull UtilsService utilsService,
                                 @NonNull RequestManager requestManager,
                                 @NonNull AttributesProperties attributesProperties,
                                 @NonNull AppBeansContainer appBeansContainer,
                                 @NonNull ApplicationProperties applicationProperties,
                                 @NonNull ProvidedServiceManager providedServiceManager,
                                 @NonNull Map<String, AttrInput> attrInputMap,
                                 @NonNull InputsContainer inputsContainer)
    {
        this.perunAdapter = perunAdapter;
        this.utilsService = utilsService;
        this.requestManager = requestManager;
        this.attributesProperties = attributesProperties;
        this.appBeansContainer = appBeansContainer;
        this.applicationProperties = applicationProperties;
        this.providedServiceManager = providedServiceManager;
        this.attrInputMap = attrInputMap;
        this.inputsContainer = inputsContainer;
    }

    @Override
    public Facility getFacility(@NonNull Long facilityId, @NonNull Long userId,
                                boolean checkAdmin, boolean includeClientCredentials)
            throws UnauthorizedActionException, InternalErrorException, BadPaddingException, InvalidKeyException,
            IllegalBlockSizeException, PerunUnknownException, PerunConnectionException
    {
        if (checkAdmin && !utilsService.isAdminForFacility(facilityId, userId)) {
            throw new UnauthorizedActionException("User cannot view facility, user is not an admin");
        }

        Facility facility = perunAdapter.getFacilityById(facilityId);
        if (facility == null) {
            throw new InternalErrorException("Could not retrieve facility for id: " + facilityId);
        }

        Long activeRequestId = requestManager.getActiveRequestIdByFacilityId(facilityId);

        List<String> attrsToFetch = new ArrayList<>(appBeansContainer.getAllAttrNames());
        Map<String, PerunAttribute> attrs = perunAdapter.getFacilityAttributes(facilityId, attrsToFetch);
        boolean isOidc = ServiceUtils.isOidcAttributes(attrs, attributesProperties.getNames().getEntityId());
        List<String> keptAttrs = this.getAttrsToKeep(isOidc);
        List<PerunAttribute> filteredAttributes = ServiceUtils.filterFacilityAttrs(attrs, keptAttrs);
        Map<AttributeCategory, Map<String, PerunAttribute>> facilityAttributes =
                this.convertToCategoryMap(filteredAttributes);

        facility.setName(ServiceUtils.extractFacilityName(facility, attributesProperties));
        facility.setDescription(ServiceUtils.extractFacilityDescription(facility, attributesProperties));
        facility.setAttributes(facilityAttributes);
        facility.setActiveRequestId(activeRequestId);
        facility.setTestEnv(attrs.get(attributesProperties.getNames().getIsTestSp()).valueAsBoolean());

        if (isOidc && includeClientCredentials) {
            String clientSecretValue = this.extractClientSecretValueDecrypted(facility);
            facility.getAttributes()
                    .get(AttributeCategory.PROTOCOL)
                    .get(attributesProperties.getNames().getOidcClientId())
                    .setValue(JsonNodeFactory.instance.textNode(clientSecretValue));
        }

        if (!includeClientCredentials) {
            this.clearOidcCredentials(facility);
        }

        Map<String, PerunAttribute> additionalAttributes = perunAdapter.getFacilityAttributes(facility.getId(),
                Arrays.asList(
                        attributesProperties.getNames().getIsOidc(),
                        attributesProperties.getNames().getIsSaml(),
                        attributesProperties.getNames().getMasterProxyIdentifier()
                ));

        this.fillProtocolAttributes(facility, additionalAttributes);
        this.fillFacilityEditable(facility, additionalAttributes);
        return facility;
    }

    @Override
    public Facility getFacilityWithInputs(@NonNull Long facilityId, @NonNull Long userId)
            throws UnauthorizedActionException, InternalErrorException, BadPaddingException, InvalidKeyException,
            IllegalBlockSizeException, PerunUnknownException, PerunConnectionException
    {

        Facility facility = this.getFacility(facilityId, userId, true, false);
        if (facility == null || facility.getAttributes() == null) {
            throw new InternalErrorException("Could not fetch facility for id: " + facilityId);
        }

        facility.getAttributes()
                .values()
                .forEach(
                        attrsInCategory -> attrsInCategory.values()
                                .forEach(attr -> attr.setInput(attrInputMap.get(attr.getFullName())))
                );

        return facility;
    }

    @Override
    public List<ProvidedService> getAllUserFacilities(@NonNull Long userId)
            throws PerunUnknownException, PerunConnectionException
    {
        List<Facility> proxyFacilities = this.getFacilitiesByProxyIdentifier();
        if (proxyFacilities == null || proxyFacilities.isEmpty()) {
            return new ArrayList<>();
        }

        List<Facility> userFacilities = perunAdapter.getFacilitiesWhereUserIsAdmin(userId);
        if (userFacilities == null || userFacilities.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> proxyFacilitiesIds = proxyFacilities.stream()
                .map(PerunEntity::getId)
                .collect(Collectors.toSet());

        Set<Long> userFacilitiesIds = proxyFacilities.stream()
                .map(PerunEntity::getId)
                .collect(Collectors.toSet());

        proxyFacilitiesIds.retainAll(userFacilitiesIds);
        if (proxyFacilitiesIds.isEmpty()) {
            return new ArrayList<>();
        }

        return providedServiceManager.getAllForFacilities(proxyFacilitiesIds);
    }

    @Override
    public List<ProvidedService> getAllFacilities(@NonNull Long userId) throws UnauthorizedActionException {
        if (!applicationProperties.isAppAdmin(userId)) {
            throw new UnauthorizedActionException("User cannot list all facilities, user does not have role APP_ADMIN");
        }

        return providedServiceManager.getAll();
    }

    // private methods

    private void clearOidcCredentials(Facility facility) {
        facility.getAttributes().get(AttributeCategory.PROTOCOL)
                .remove(attributesProperties.getNames().getOidcClientId());
        facility.getAttributes().get(AttributeCategory.PROTOCOL)
                .remove(attributesProperties.getNames().getOidcClientSecret());
    }

    private List<String> getAttrsToKeep(boolean isOidc) {
        List<String> keptAttrs = new LinkedList<>();

        keptAttrs.addAll(inputsContainer.getServiceInputs()
                .stream().map(AttrInput::getName).collect(Collectors.toList()));
        keptAttrs.addAll(inputsContainer.getOrganizationInputs()
                .stream().map(AttrInput::getName).collect(Collectors.toList()));
        keptAttrs.addAll(inputsContainer.getMembershipInputs()
                .stream().map(AttrInput::getName).collect(Collectors.toList()));

        if (isOidc) {
            keptAttrs.addAll(inputsContainer.getOidcInputs()
                    .stream().map(AttrInput::getName).collect(Collectors.toList()));
            keptAttrs.add(attributesProperties.getNames().getOidcClientId());
            keptAttrs.add(attributesProperties.getNames().getOidcClientSecret());
        } else {
            keptAttrs.addAll(inputsContainer.getSamlInputs()
                    .stream().map(AttrInput::getName).collect(Collectors.toList()));
        }

        return keptAttrs;
    }

    private Map<AttributeCategory, Map<String, PerunAttribute>> convertToCategoryMap(List<PerunAttribute> filteredAttributes)
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
                AttributeCategory category = appBeansContainer.getAttrCategory(attribute.getFullName());
                attribute.setInput(attrInputMap.get(attribute.getFullName()));
                map.get(category).put(attribute.getFullName(), attribute);
            }
        }

        return map;
    }

    private String extractClientSecretValueDecrypted(Facility facility)
            throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException
    {
        PerunAttribute clientSecret = facility.getAttributes()
                .get(AttributeCategory.PROTOCOL)
                .get(attributesProperties.getNames().getOidcClientId());
        String valEncrypted = clientSecret.valueAsString();
        return ServiceUtils.decrypt(valEncrypted, appBeansContainer.getSecretKeySpec());
    }

    private void fillFacilityEditable(Facility facility, Map<String, PerunAttribute> additionalAttributes) {
        if (additionalAttributes != null
                && additionalAttributes.containsKey(attributesProperties.getNames().getMasterProxyIdentifier())) {
            PerunAttribute masterProxyIdentifierAttribute = additionalAttributes
                    .get(attributesProperties.getNames().getMasterProxyIdentifier());

            if (masterProxyIdentifierAttribute != null) {
                facility.setEditable(masterProxyIdentifierAttribute.valueAsBoolean());
            }
        }
    }

    private void fillProtocolAttributes(Facility facility, Map<String, PerunAttribute> attributeMap) {
        if (attributeMap.containsKey(attributesProperties.getNames().getIsOidc())) {
            PerunAttribute isOidc = attributeMap.get(attributesProperties.getNames().getIsOidc());
            if (isOidc != null) {
                facility.setOidc(attributeMap.get(attributesProperties.getNames().getIsOidc()).valueAsBoolean());
            }
        }

        if (attributeMap.containsKey(attributesProperties.getNames().getIsSaml())) {
            PerunAttribute isSaml = attributeMap.get(attributesProperties.getNames().getIsSaml());
            if (isSaml != null) {
                facility.setSaml(attributeMap.get(attributesProperties.getNames().getIsSaml()).valueAsBoolean());
            }
        }

    }

    private List<Facility> getFacilitiesByProxyIdentifier() throws PerunUnknownException, PerunConnectionException {
        return perunAdapter.getFacilitiesByProxyIdentifier(
                attributesProperties.getNames().getProxyIdentifier(),
                attributesProperties.getValues().getProxyIdentifier()
        );
    }

}
