package cz.metacentrum.perun.spRegistration.service.impl;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.metacentrum.perun.spRegistration.common.configs.AppBeansContainer;
import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.Request;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.persistence.managers.LinkCodeManager;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestManager;
import cz.metacentrum.perun.spRegistration.service.MailsService;
import cz.metacentrum.perun.spRegistration.service.ServiceUtils;
import cz.metacentrum.perun.spRegistration.service.UtilsService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cz.metacentrum.perun.spRegistration.service.impl.MailsServiceImpl.LANG_CS;
import static cz.metacentrum.perun.spRegistration.service.impl.MailsServiceImpl.LANG_EN;

@Service("utilsService")
@Slf4j
public class UtilsServiceImpl implements UtilsService {

    @NonNull private final LinkCodeManager linkCodeManager;
    @NonNull private final PerunAdapter perunAdapter;
    @NonNull private final MailsService mailsService;
    @NonNull private final ApplicationProperties applicationProperties;
    @NonNull private final AttributesProperties attributesProperties;
    @NonNull private final AppBeansContainer appBeansContainer;
    @NonNull private final RequestManager requestManager;

    @Autowired
    public UtilsServiceImpl(@NonNull LinkCodeManager linkCodeManager,
                            @NonNull PerunAdapter perunAdapter,
                            @NonNull MailsServiceImpl mailsService,
                            @NonNull ApplicationProperties applicationProperties,
                            @NonNull AttributesProperties attributesProperties,
                            @NonNull AppBeansContainer appBeansContainer,
                            @NonNull RequestManager requestManager)
    {
        this.linkCodeManager = linkCodeManager;
        this.perunAdapter = perunAdapter;
        this.mailsService = mailsService;
        this.applicationProperties = applicationProperties;
        this.attributesProperties = attributesProperties;
        this.appBeansContainer = appBeansContainer;
        this.requestManager = requestManager;
    }

    @Override
    public boolean validateCode(@NonNull String code) {
        return (null != linkCodeManager.get(code));
    }

    @Override
    public PerunAttribute regenerateClientSecret(@NonNull Long userId, @NonNull Long facilityId)
            throws BadPaddingException, InvalidKeyException,
            IllegalBlockSizeException, PerunUnknownException, PerunConnectionException
    {
        PerunAttribute clientSecret = this.generateClientSecretAttribute();
        perunAdapter.setFacilityAttribute(facilityId, clientSecret.toJson());

        String decrypted = ServiceUtils.decrypt(clientSecret.valueAsString(), appBeansContainer.getSecretKeySpec());
        clientSecret.setValue(clientSecret.getDefinition().getType(), JsonNodeFactory.instance.textNode(decrypted));

        Facility facility = null;
        try {
            facility = perunAdapter.getFacilityById(facilityId);
        } catch (PerunConnectionException e) {
            log.warn("Could not fetch facility, attribute has been set. Remaining actions - send notification." +
                    "Setting FacilityID as name and description");
        }
        if (facility == null) {
            facility = new Facility(facilityId, facilityId.toString(), facilityId.toString());
            Map<String, String> map = new HashMap<>();
            map.put(LANG_EN, facilityId.toString());
            map.put(LANG_CS, facilityId.toString());
            facility.setName(map);
            facility.setDescription(map);

        } else {
            Map<String, PerunAttribute> attrs = perunAdapter.getFacilityAttributes(facilityId, Arrays.asList(
                    attributesProperties.getNames().getServiceName(), attributesProperties.getNames().getServiceDesc())
            );

            facility.setName(ServiceUtils.extractFacilityName(attrs, attributesProperties));
            facility.setDescription(ServiceUtils.extractFacilityDescription(attrs, attributesProperties));
        }

        mailsService.notifyClientSecretChanged(facility);
        return clientSecret;
    }

    @Override
    public PerunAttribute generateClientSecretAttribute()
            throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException
    {
        PerunAttribute attribute = new PerunAttribute();
        String clientSecret = ServiceUtils.generateClientSecret();
        String encryptedClientSecret = ServiceUtils.encrypt(clientSecret, appBeansContainer.getSecretKeySpec());

        attribute.setDefinition(appBeansContainer.getAttrDefinition(attributesProperties.getNames().getOidcClientSecret()));
        attribute.setValue(attribute.getDefinition().getType(), JsonNodeFactory.instance.textNode(encryptedClientSecret));
        return attribute;
    }

    @Override
    public boolean isAdminForFacility(@NonNull Long facilityId, @NonNull Long userId)
            throws PerunUnknownException, PerunConnectionException
    {
        if (isAppAdmin(userId)) {
            return true;
        }

        Set<Long> whereAdmin = perunAdapter.getFacilityIdsWhereUserIsAdmin(userId);

        if (whereAdmin == null || whereAdmin.isEmpty()) {
            return false;
        }

        return whereAdmin.contains(facilityId);
    }

    @Override
    public boolean isAdminForFacility(@NonNull Long facilityId, @NonNull User user)
            throws PerunUnknownException, PerunConnectionException
    {
        return this.isAdminForFacility(facilityId, user.getId());
    }

    @Override
    public boolean isAdminForRequest(@NonNull Request request, @NonNull Long userId)
            throws PerunUnknownException, PerunConnectionException
    {
        if (isAppAdmin(userId)) {
            return true;
        }
        boolean res = Objects.equals(request.getReqUserId(), userId);
        if (!res && request.getFacilityId() != null) {
            res = this.isAdminForFacility(request.getFacilityId(), userId);
        }
        return res;
    }

    @Override
    public boolean isAdminForRequest(@NonNull Long reqId, @NonNull Long userId)
            throws PerunUnknownException, PerunConnectionException, InternalErrorException {
        if (isAppAdmin(userId)) {
            return true;
        }
        Request request = requestManager.getRequestById(reqId);
        if (request == null) {
            throw new InternalErrorException("Unknown request");
        }
        boolean res = Objects.equals(request.getReqUserId(), userId);
        if (!res && request.getFacilityId() != null) {
            res = this.isAdminForFacility(request.getFacilityId(), userId);
        }
        return res;
    }

    @Override
    public boolean isAdminForRequest(@NonNull Long reqId, @NonNull User user)
            throws PerunUnknownException, PerunConnectionException, InternalErrorException
    {
        return this.isAdminForRequest(reqId, user.getId());
    }

    @Override
    public boolean isAdminForRequest(@NonNull Request request, @NonNull User user)
            throws PerunUnknownException, PerunConnectionException
    {
        return this.isAdminForRequest(request, user.getId());
    }

    @Override
    public boolean isAppAdmin(@NonNull Long userId) {
        return applicationProperties.isAppAdmin(userId);
    }

    @Override
    public boolean isAppAdmin(@NonNull User user) {
        return this.isAppAdmin(user.getId());
    }

}
