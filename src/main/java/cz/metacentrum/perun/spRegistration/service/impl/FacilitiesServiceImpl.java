package cz.metacentrum.perun.spRegistration.service.impl;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.metacentrum.perun.spRegistration.common.configs.AppBeansContainer;
import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.models.AttrInput;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.InputsContainer;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.PerunEntity;
import cz.metacentrum.perun.spRegistration.common.models.ProvidedService;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.persistence.managers.ProvidedServiceManager;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestManager;
import cz.metacentrum.perun.spRegistration.service.FacilitiesService;
import cz.metacentrum.perun.spRegistration.service.ServiceUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cz.metacentrum.perun.spRegistration.persistence.enums.ServiceProtocol.OIDC;

@Service("facilitiesService")
@Slf4j
public class FacilitiesServiceImpl implements FacilitiesService {

    @NonNull private final PerunAdapter perunAdapter;
    @NonNull private final RequestManager requestManager;
    @NonNull private final AttributesProperties attributesProperties;
    @NonNull private final AppBeansContainer applicationBeans;
    @NonNull private final ProvidedServiceManager providedServiceManager;
    @NonNull private final Map<String, AttrInput> attrInputMap;
    @NonNull private final InputsContainer inputsContainer;

    @Autowired
    public FacilitiesServiceImpl(@NonNull PerunAdapter perunAdapter,
                                 @NonNull RequestManager requestManager,
                                 @NonNull AttributesProperties attributesProperties,
                                 @NonNull AppBeansContainer applicationBeans,
                                 @NonNull ProvidedServiceManager providedServiceManager,
                                 @NonNull Map<String, AttrInput> attrInputMap,
                                 @NonNull InputsContainer inputsContainer)
    {
        this.perunAdapter = perunAdapter;
        this.requestManager = requestManager;
        this.attributesProperties = attributesProperties;
        this.applicationBeans = applicationBeans;
        this.providedServiceManager = providedServiceManager;
        this.attrInputMap = attrInputMap;
        this.inputsContainer = inputsContainer;
    }

    @Override
    public Facility getFacility(@NonNull Long facilityId, @NonNull Long userId, boolean includeClientCredentials)
            throws InternalErrorException, BadPaddingException, InvalidKeyException,
            IllegalBlockSizeException, PerunUnknownException, PerunConnectionException
    {
        ProvidedService service = providedServiceManager.getByFacilityId(facilityId);
        if (service == null) {
            throw new InternalErrorException("Could not retrieve service for ID: " + facilityId);
        }
        if (!service.isFacilityDeleted()) {
            Facility facility = perunAdapter.getFacilityById(facilityId);
            if (facility == null) {
                throw new IllegalArgumentException("Could not retrieve facility for id: " + facilityId);
            }

            facility.setName(service.getName());
            facility.setDescription(service.getDescription());
            facility.setEnvironment(service.getEnvironment());
            facility.setProtocol(service.getProtocol());
            facility.setActiveRequestId(requestManager.getActiveRequestIdByFacilityId(facilityId));
            facility.setAttributes(getFacilityAttributes(facilityId));

            if (!includeClientCredentials) {
                clearOidcCredentials(facility);
            } else {
                if (OIDC.equals(facility.getProtocol())) {
                    decryptClientSecretValueInAttr(facility);
                }
            }

            return facility;
        } else {
            // facility has been deleted, return some artifact reconstructed from provided service object
            Facility f = new Facility(service.getFacilityId());
            f.setPerunName(service.getName().get("en"));
            f.setPerunDescription(service.getDescription().get("en"));
            f.setName(service.getName());
            f.setDescription(service.getDescription());
            f.setEnvironment(service.getEnvironment());
            f.setProtocol(service.getProtocol());
            f.setDeleted(true);
            return f;
        }
    }

    @Override
    public Facility getFacilityWithInputs(@NonNull Long facilityId, @NonNull Long userId)
            throws InternalErrorException, BadPaddingException, InvalidKeyException,
            IllegalBlockSizeException, PerunUnknownException, PerunConnectionException
    {
        Facility facility = getFacility(facilityId, userId, true);
        if (facility == null) {
            throw new IllegalArgumentException("Could not fetch facility for id: " + facilityId);
        } else if (facility.getAttributes() == null) {
            throw new InternalErrorException("Could not fetch attributes");
        }

        facility.getAttributes().values().forEach(attrsInCategory -> attrsInCategory.values().forEach(
                attr -> attr.setInput(attrInputMap.get(attr.getFullName())))
        );

        return facility;
    }

    @Override
    public List<ProvidedService> getAllUserFacilities(@NonNull Long userId)
            throws PerunUnknownException, PerunConnectionException
    {
        List<Facility> proxyFacilities = getFacilitiesByProxyIdentifier();
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

        Set<Long> userFacilitiesIds = userFacilities.stream()
                .map(PerunEntity::getId)
                .collect(Collectors.toSet());

        proxyFacilitiesIds.retainAll(userFacilitiesIds);
        if (proxyFacilitiesIds.isEmpty()) {
            return new ArrayList<>();
        }

        return providedServiceManager.getAllForFacilities(proxyFacilitiesIds);
    }

    @Override
    public List<ProvidedService> getAllFacilities(@NonNull Long userId) {
        return providedServiceManager.getAll();
    }

    @Override
    public Facility getFacilityForSignature(@NonNull Long facilityId, @NonNull Long userId)
            throws BadPaddingException, PerunUnknownException, IllegalBlockSizeException, PerunConnectionException,
            InternalErrorException, InvalidKeyException
    {
        Facility facility = getFacility(facilityId, userId, true);
        if (facility == null) {
            throw new IllegalArgumentException("Could not fetch facility for ID " + facilityId);
        } else if (facility.getAttributes() == null) {
            throw new InternalErrorException();
        }
        facility.getAttributes().get(AttributeCategory.PROTOCOL).clear();
        facility.getAttributes().get(AttributeCategory.ACCESS_CONTROL).clear();
        return facility;
    }

    // private methods

    private void clearOidcCredentials(Facility facility) {
        facility.getAttributes().get(AttributeCategory.PROTOCOL)
                .remove(attributesProperties.getNames().getOidcClientId());
        facility.getAttributes().get(AttributeCategory.PROTOCOL)
                .remove(attributesProperties.getNames().getOidcClientSecret());
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
                AttributeCategory category = applicationBeans.getAttrCategory(attribute.getFullName());
                attribute.setInput(attrInputMap.get(attribute.getFullName()));
                map.get(category).put(attribute.getFullName(), attribute);
            }
        }

        return map;
    }

    private List<Facility> getFacilitiesByProxyIdentifier() throws PerunUnknownException, PerunConnectionException {
        return perunAdapter.getFacilitiesByProxyIdentifier(
                attributesProperties.getNames().getProxyIdentifier(),
                attributesProperties.getValues().getProxyIdentifier()
        );
    }

    private Map<AttributeCategory, Map<String, PerunAttribute>> getFacilityAttributes(Long facilityId)
            throws PerunUnknownException, PerunConnectionException
    {
        return convertToCategoryMap(ServiceUtils.getFacilityAttributes(applicationBeans, facilityId,
                attributesProperties, inputsContainer, perunAdapter));
    }

    private void decryptClientSecretValueInAttr(Facility facility)
            throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException
    {
        PerunAttribute clientSecret = facility.getAttributes()
                .get(AttributeCategory.PROTOCOL)
                .get(attributesProperties.getNames().getOidcClientSecret());
        String clientSecretValue = ServiceUtils.decrypt(clientSecret.valueAsString(),
                applicationBeans.getSecretKeySpec());
        facility.getAttributes()
                .get(AttributeCategory.PROTOCOL)
                .get(attributesProperties.getNames().getOidcClientSecret())
                .setValue(clientSecret.getDefinition().getType(),
                        JsonNodeFactory.instance.textNode(clientSecretValue));
    }

}
