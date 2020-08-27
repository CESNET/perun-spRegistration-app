package cz.metacentrum.perun.spRegistration.service.impl;

import cz.metacentrum.perun.spRegistration.Utils;
import cz.metacentrum.perun.spRegistration.common.configs.AppConfig;
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
public class UtilsServiceImpl implements UtilsService {

    private static final Logger log = LoggerFactory.getLogger(UtilsServiceImpl.class);

    private final LinkCodeManager linkCodeManager;
    private final PerunAdapter perunAdapter;
    private final AppConfig appConfig;
    private final MailsService mailsService;

    @Autowired
    public UtilsServiceImpl(LinkCodeManager linkCodeManager, PerunAdapter perunAdapter, AppConfig appConfig,
                            MailsServiceImpl mailsService)
    {
        this.linkCodeManager = linkCodeManager;
        this.perunAdapter = perunAdapter;
        this.appConfig = appConfig;
        this.mailsService = mailsService;
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
        } else if (!appConfig.isAppAdmin(userId) && !isFacilityAdmin(facilityId, userId)) {
            log.error("User is not authorized to regenerate client secret");
            throw new UnauthorizedActionException("User is not authorized to regenerate client secret");
        }

        PerunAttribute clientSecret = generateClientSecretAttribute();
        perunAdapter.setFacilityAttribute(facilityId, clientSecret.toJson());

        String decrypted = ServiceUtils.decrypt(clientSecret.valueAsString(), appConfig.getSecret());
        clientSecret.setValue(decrypted);

        Facility facility = perunAdapter.getFacilityById(facilityId);
        Map<String, PerunAttribute> attrs = perunAdapter.getFacilityAttributes(facilityId, Arrays.asList(
                appConfig.getServiceNameAttributeName(), appConfig.getServiceDescAttributeName()));

        facility.setName(attrs.get(appConfig.getServiceNameAttributeName()).valueAsMap());
        facility.setDescription(attrs.get(appConfig.getServiceDescAttributeName()).valueAsMap());

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

        if (appConfig.isAppAdmin(userId)) {
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
        attribute.setDefinition(appConfig.getAttrDefinition(appConfig.getClientSecretAttribute()));

        String clientSecret = ServiceUtils.generateClientSecret();
        String encryptedClientSecret = ServiceUtils.encrypt(clientSecret, appConfig.getSecret());

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

        boolean res = reqUserId.equals(userId) || appConfig.isAppAdmin(userId);

        log.debug("isAdminInRequest returns: {}", res);
        return res;
    }

}
