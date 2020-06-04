package cz.metacentrum.perun.spRegistration.service.impl;

import cz.metacentrum.perun.spRegistration.Utils;
import cz.metacentrum.perun.spRegistration.common.configs.AppConfig;
import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.exceptions.ConnectorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.ExpiredCodeException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.LinkCode;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.connectors.PerunConnector;
import cz.metacentrum.perun.spRegistration.persistence.managers.LinkCodeManager;
import cz.metacentrum.perun.spRegistration.service.AddAdminsService;
import cz.metacentrum.perun.spRegistration.service.FacilitiesService;
import cz.metacentrum.perun.spRegistration.service.MailsService;
import cz.metacentrum.perun.spRegistration.service.ServiceUtils;
import cz.metacentrum.perun.spRegistration.service.UtilsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("addAdminsService")
public class AddAdminsServiceImpl implements AddAdminsService {

    private static final Logger log = LoggerFactory.getLogger(AddAdminsServiceImpl.class);

    private final PerunConnector perunConnector;
    private final AppConfig appConfig;
    private final MailsService mailsService;
    private final LinkCodeManager linkCodeManager;
    private final UtilsService utilsService;
    private final FacilitiesService facilitiesService;

    @Autowired
    public AddAdminsServiceImpl(PerunConnector perunConnector, AppConfig appConfig, MailsService mailsService,
                                LinkCodeManager linkCodeManager, UtilsService utilsService, FacilitiesService facilitiesService) {
        this.perunConnector = perunConnector;
        this.appConfig = appConfig;
        this.mailsService = mailsService;
        this.linkCodeManager = linkCodeManager;
        this.utilsService = utilsService;
        this.facilitiesService = facilitiesService;
    }

    @Override
    public boolean addAdminsNotify(User user, Long facilityId, List<String> admins)
            throws UnauthorizedActionException, ConnectorException, UnsupportedEncodingException, InternalErrorException
    {
        log.trace("addAdminsNotify(user: {}, facilityId: {}, admins: {}", user, facilityId, admins);

        if (Utils.checkParamsInvalid(user, facilityId, admins) || admins.isEmpty()) {
            log.error("Wrong parameters passed (user: {}, facilityId: {}, admins: {})", user, facilityId, admins);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        } else if (! utilsService.isFacilityAdmin(facilityId, user.getId())) {
            log.error("User cannot request adding admins to facility, user is not an admin");
            throw new UnauthorizedActionException("User cannot request adding admins to facility, user is not an admin");
        }

        Facility facility = perunConnector.getFacilityById(facilityId);
        if (facility == null) {
            log.error("Could not fetch facility for id: {}", facilityId);
            throw new InternalErrorException("Could not find facility for id: " + facilityId);
        }

        Map<String, PerunAttribute> attrs = perunConnector.getFacilityAttributes(facility.getId(),
                Arrays.asList(appConfig.getServiceNameAttributeName(), appConfig.getServiceDescAttributeName()));

        facility.setName(attrs.get(appConfig.getServiceNameAttributeName()).valueAsMap());
        facility.setDescription(attrs.get(appConfig.getServiceDescAttributeName()).valueAsMap());

        Map<String, String> adminCodeMap = generateCodesForAdmins(admins, user, facilityId);
        Map<String, String> adminLinkMap = generateLinksForAdmins(adminCodeMap);
        boolean successful = mailsService.notifyNewAdmins(facility, adminLinkMap, user);

        log.debug("addAdminsNotify returns: {}", successful);
        return successful;
    }

    @Override
    public boolean confirmAddAdmin(User user, String code)
            throws ExpiredCodeException, ConnectorException, InternalErrorException {
        log.debug("confirmAddAdmin({})", code);

        if (Utils.checkParamsInvalid(user, code)) {
            log.error("Wrong parameters passed: (user: {}, code: {})", user, code);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        LinkCode linkCode = linkCodeManager.get(code);

        if (linkCode == null) {
            log.error("User trying to become admin with invalid code: {}", code);
            throw new ExpiredCodeException("Code is invalid");
        }

        Long memberId = perunConnector.getMemberIdByUser(appConfig.getSpAdminsRootVoId(), user.getId());
        if (memberId == null) {
            throw new InternalErrorException("No member could be found for user");
        }
        PerunAttribute adminsGroupAttribute = perunConnector.getFacilityAttribute(linkCode.getFacilityId(), appConfig.getAdminsGroupAttribute());
        if (adminsGroupAttribute == null || adminsGroupAttribute.valueAsLong() == null) {
            throw new InternalErrorException("No admins group found for service");
        }

        boolean added = perunConnector.addMemberToGroup(adminsGroupAttribute.valueAsLong(), memberId);
        if (!added) {
            log.error("some operations failed: added: false");
        } else {
            linkCodeManager.delete(code);
            log.info("Admin added, code deleted");
        }

        log.debug("confirmAddAdmin returns: {}", added);
        return added;
    }

    @Override
    public void rejectAddAdmin(User user, String code) throws ExpiredCodeException, InternalErrorException {
        log.debug("rejectAddAdmin(user: {}, code: {})", user, code);

        if (Utils.checkParamsInvalid(user, code)) {
            log.error("Wrong parameters passed: (user: {}, code: {})", user, code);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        LinkCode linkCode = linkCodeManager.get(code);

        if (linkCode == null) {
            log.error("User trying to become reject becoming with invalid code: {}", code);
            throw new ExpiredCodeException("Code is invalid");
        }

        linkCodeManager.delete(code);
        log.debug("rejectAddAdmin() returns");
    }

    @Override
    public LinkCode getDetails(String hash) {
        log.debug("getDetails({})", hash);

        LinkCode code = linkCodeManager.get(hash);

        log.debug("getDetails({}) returns: {}", hash, code);
        return code;
    }

    @Override
    public Facility getFacilityDetails(Long facilityId, User user) throws BadPaddingException, InvalidKeyException,
            ConnectorException, IllegalBlockSizeException, InternalErrorException, UnauthorizedActionException
    {
        log.debug("getFacilityDetails({}, {})", facilityId, user);

        Facility facility = facilitiesService.getFacility(facilityId, user.getId(), false, false);
        facility.getAttributes().get(AttributeCategory.PROTOCOL).clear();
        facility.getAttributes().get(AttributeCategory.ACCESS_CONTROL).clear();

        log.debug("getFacilityDetails({}, {}) returns: {}", facilityId, user, facility);
        return facility;
    }

    private Map<String, String> generateCodesForAdmins(List<String> admins, User user, Long facility)
            throws InternalErrorException
    {
        log.trace("generateCodesForAdmins(admins: {}, user: {}, facility: {})", admins, user, facility);

        if (Utils.checkParamsInvalid(admins, user, facility)) {
            log.error("Wrong parameters passed: (admins: {}, user: {}, facility: {})", admins, user, facility);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        List<LinkCode> codes = new ArrayList<>();
        Map<String, String> adminCodesMap = new HashMap<>();

        for (String admin : admins) {
            LinkCode code = createAddAdminCode(admin, user, facility);
            codes.add(code);
            adminCodesMap.put(admin, code.getHash());
        }

        linkCodeManager.createMultiple(codes);

        log.trace("generateCodesForAdmins() returns: {}", adminCodesMap);
        return adminCodesMap;
    }

    private Map<String, String> generateLinksForAdmins(Map<String, String> adminCodeMap)
            throws UnsupportedEncodingException
    {
        log.trace("generateLinksForAdmins(adminCodeMap: {})", adminCodeMap);

        if (Utils.checkParamsInvalid(adminCodeMap)) {
            log.error("Wrong parameters passed: (adminCodeMap: {})", adminCodeMap);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        Map<String, String> linksMap = new HashMap<>();

        for (Map.Entry<String, String> entry : adminCodeMap.entrySet()) {
            String code = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString());
            String link = appConfig.getAdminsEndpoint()
                    .concat("?code=").concat(code);
            linksMap.put(entry.getKey(), link);
            log.debug("Generated code: {}", code); //TODO: remove
        }

        log.trace("generateLinksForAdmins() returns: {}", linksMap);
        return linksMap;
    }

    private LinkCode createAddAdminCode(String admin, User user, Long facility) {
        log.trace("createRequestCode(admin: {}, user: {}, facility: {})", admin, user, facility);
        LinkCode code = new LinkCode();

        code.setRecipientEmail(admin);
        code.setSenderName(user.getName());
        code.setSenderEmail(user.getEmail());
        code.setExpiresAt(appConfig);
        code.setFacilityId(facility);
        code.setRequestId(-1L);
        code.setHash(ServiceUtils.getHash(code.toString()));

        log.trace("createRequestCode(admin: {}, user: {}, facility: {}) returns: {}", admin, user, facility, facility);
        return code;
    }
}
