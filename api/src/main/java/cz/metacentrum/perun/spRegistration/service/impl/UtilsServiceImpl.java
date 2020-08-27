package cz.metacentrum.perun.spRegistration.service.impl;

import cz.metacentrum.perun.spRegistration.Utils;
import cz.metacentrum.perun.spRegistration.common.configs.ApplicationBeans;
import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.common.exceptions.CodeNotStoredException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.persistence.managers.LinkCodeManager;
import cz.metacentrum.perun.spRegistration.service.MailsService;
import cz.metacentrum.perun.spRegistration.service.ServiceUtils;
import cz.metacentrum.perun.spRegistration.service.UtilsService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

@Service("utilsService")
@Slf4j
public class UtilsServiceImpl implements UtilsService {

    private final LinkCodeManager linkCodeManager;
    private final PerunAdapter perunAdapter;
    private final MailsService mailsService;
    private final ApplicationProperties applicationProperties;
    private final AttributesProperties attributesProperties;
    private final ApplicationBeans applicationBeans;

    @Autowired
    public UtilsServiceImpl(LinkCodeManager linkCodeManager, PerunAdapter perunAdapter, MailsServiceImpl mailsService,
                            ApplicationProperties applicationProperties, AttributesProperties attributesProperties,
                            ApplicationBeans applicationBeans)
    {
        this.linkCodeManager = linkCodeManager;
        this.perunAdapter = perunAdapter;
        this.mailsService = mailsService;
        this.applicationProperties = applicationProperties;
        this.attributesProperties = attributesProperties;
        this.applicationBeans = applicationBeans;
    }

    @Override
    public boolean validateCode(String code) throws CodeNotStoredException {
        log.trace("validateCode({})", code);

        if (Utils.checkParamsInvalid(code)) {
            log.error("Wrong parameters passed: (code: {})", code);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        } else if (null == linkCodeManager.get(code)) {
            throw new CodeNotStoredException("Code not found");
        }

        boolean isValid = null != linkCodeManager.get(code);

        log.trace("validateCode() returns: {}", isValid);
        return isValid;
    }

    @Override
    public PerunAttribute regenerateClientSecret(Long userId, Long facilityId) throws UnauthorizedActionException,
            BadPaddingException, InvalidKeyException, IllegalBlockSizeException, PerunUnknownException,
            PerunConnectionException
    {
        log.trace("regenerateClientSecret({}, {})", userId, facilityId);

        if (Utils.checkParamsInvalid(userId, facilityId)) {
            log.error("Wrong parameters passed: (userId: {}, facilityId: {})", userId, facilityId);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        } else if (!applicationProperties.isAppAdmin(userId) && !isFacilityAdmin(facilityId, userId)) {
            log.error("User is not authorized to regenerate client secret");
            throw new UnauthorizedActionException("User is not authorized to regenerate client secret");
        }

        PerunAttribute clientSecret = generateClientSecretAttribute();
        perunAdapter.setFacilityAttribute(facilityId, clientSecret.toJson());

        String decrypted = ServiceUtils.decrypt(clientSecret.valueAsString(), applicationBeans.getSecretKeySpec());
        clientSecret.setValue(decrypted);

        Facility facility = perunAdapter.getFacilityById(facilityId);
        Map<String, PerunAttribute> attrs = perunAdapter.getFacilityAttributes(facilityId, Arrays.asList(
                attributesProperties.getServiceNameAttrName(), attributesProperties.getServiceDescAttrName())
        );

        facility.setName(attrs.get(attributesProperties.getServiceNameAttrName()).valueAsMap());
        facility.setDescription(attrs.get(attributesProperties.getServiceDescAttrName()).valueAsMap());

        mailsService.notifyClientSecretChanged(facility);

        log.trace("regenerateClientSecret({}, {}) returns: {}", userId, facilityId, clientSecret);
        return clientSecret;
    }

    @Override
    public boolean isFacilityAdmin(Long facilityId, Long userId) throws PerunUnknownException,
            PerunConnectionException
    {
        log.trace("isFacilityAdmin(facilityId: {}, userId: {})", facilityId, userId);

        if (Utils.checkParamsInvalid(facilityId, userId)) {
            log.error("Wrong parameters passed: (facility: {}, userId: {})", facilityId, userId);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        if (applicationProperties.isAppAdmin(userId)) {
            return true;
        }

        Set<Long> whereAdmin = perunAdapter.getFacilityIdsWhereUserIsAdmin(userId);

        if (whereAdmin == null || whereAdmin.isEmpty()) {
            log.debug("isFacilityAdmin returns: {}", false);
            return false;
        }

        boolean result = whereAdmin.contains(facilityId);
        log.debug("isFacilityAdmin returns:  {}", result);
        return result;
    }

    @Override
    public PerunAttribute generateClientSecretAttribute() throws BadPaddingException, InvalidKeyException,
            IllegalBlockSizeException
    {
        log.trace("generateClientIdAttribute()");

        PerunAttribute attribute = new PerunAttribute();
        attribute.setDefinition(applicationBeans.getAttrDefinition(attributesProperties.getOidcClientSecretAttrName()));

        String clientSecret = ServiceUtils.generateClientSecret();
        String encryptedClientSecret = ServiceUtils.encrypt(clientSecret, applicationBeans.getSecretKeySpec());

        attribute.setValue(encryptedClientSecret);

        log.trace("generateClientIdAttribute() returns: {}", attribute);
        return attribute;
    }

    @Override
    public boolean isAdminInRequest(Long reqUserId, Long userId) {
        log.debug("isAdminInRequest(reqUserId: {}, userId: {})", reqUserId, userId);

        if (Utils.checkParamsInvalid(reqUserId, userId)) {
            log.error("Wrong parameters passed: (reqUserId: {}, userId: {})", reqUserId, userId);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        boolean res = reqUserId.equals(userId) || applicationProperties.isAppAdmin(userId);

        log.debug("isAdminInRequest returns: {}", res);
        return res;
    }

}
