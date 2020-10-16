package cz.metacentrum.perun.spRegistration.service.impl;

import cz.metacentrum.perun.spRegistration.common.ExecuteAndSwallowException;
import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.configs.ApprovalsProperties;
import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.exceptions.ExpiredCodeException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.LinkCode;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.persistence.managers.LinkCodeManager;
import cz.metacentrum.perun.spRegistration.service.AddAdminsService;
import cz.metacentrum.perun.spRegistration.service.FacilitiesService;
import cz.metacentrum.perun.spRegistration.service.MailsService;
import cz.metacentrum.perun.spRegistration.service.ServiceUtils;
import cz.metacentrum.perun.spRegistration.service.UtilsService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AddAdminsServiceImpl implements AddAdminsService {

    @NonNull private final PerunAdapter perunAdapter;
    @NonNull private final MailsService mailsService;
    @NonNull private final LinkCodeManager linkCodeManager;
    @NonNull private final UtilsService utilsService;
    @NonNull private final FacilitiesService facilitiesService;
    @NonNull private final AttributesProperties attributesProperties;
    @NonNull private final ApprovalsProperties approvalsProperties;
    @NonNull private final ApplicationProperties applicationProperties;

    @Autowired
    public AddAdminsServiceImpl(PerunAdapter perunAdapter,
                                MailsService mailsService,
                                LinkCodeManager linkCodeManager,
                                UtilsService utilsService,
                                FacilitiesService facilitiesService,
                                AttributesProperties attributesProperties,
                                ApprovalsProperties approvalsProperties,
                                ApplicationProperties applicationProperties)
    {
        this.perunAdapter = perunAdapter;
        this.attributesProperties = attributesProperties;
        this.mailsService = mailsService;
        this.linkCodeManager = linkCodeManager;
        this.utilsService = utilsService;
        this.facilitiesService = facilitiesService;
        this.approvalsProperties = approvalsProperties;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public boolean addAdminsNotify(@NonNull User user, @NonNull Long facilityId, @NonNull List<String> admins)
            throws UnauthorizedActionException, UnsupportedEncodingException, InternalErrorException,
            PerunUnknownException, PerunConnectionException
    {
        if (!utilsService.isAdminForFacility(facilityId, user.getId())) {
            throw new UnauthorizedActionException("User cannot request adding admins to facility, user is not an admin");
        }

        Facility facility = perunAdapter.getFacilityById(facilityId);
        if (facility == null) {
            throw new InternalErrorException("Could not find facility for id: " + facilityId);
        }

        Map<String, PerunAttribute> attrs = perunAdapter.getFacilityAttributes(facility.getId(), Arrays.asList(
                attributesProperties.getNames().getServiceName(), attributesProperties.getNames().getServiceDesc())
        );

        if (attrs != null) {
            facility.setName(attrs.get(attributesProperties.getNames().getServiceName()).valueAsMap());
            facility.setDescription(attrs.get(attributesProperties.getNames().getServiceDesc()).valueAsMap());
        }

        Map<String, String> adminCodeMap = this.generateCodesForAdmins(admins, user, facilityId);
        Map<String, String> adminLinkMap = this.generateLinksForAdmins(adminCodeMap);
        return mailsService.notifyNewAdmins(facility, adminLinkMap, user);
    }

    @Override
    public boolean confirmAddAdmin(@NonNull User user, @NonNull String code)
            throws ExpiredCodeException, InternalErrorException, PerunUnknownException, PerunConnectionException
    {
        LinkCode linkCode = linkCodeManager.get(code);
        if (linkCode == null) {
            throw new ExpiredCodeException("Code is invalid");
        }

        Long memberId = perunAdapter.getMemberIdByUser(applicationProperties.getSpAdminsVoId(), user.getId());
        if (memberId == null) {
            throw new InternalErrorException("No member could be found for user");
        }
        PerunAttribute adminsGroupAttribute = perunAdapter.getFacilityAttribute(
                linkCode.getFacilityId(), attributesProperties.getNames().getManagerGroup());
        if (adminsGroupAttribute == null || adminsGroupAttribute.valueAsLong() == null) {
            throw new InternalErrorException("No admins group found for service");
        }
        boolean added = perunAdapter.addMemberToGroup(adminsGroupAttribute.valueAsLong(), memberId);
        if (added) {
            try {
                linkCodeManager.delete(code);
            } catch (Exception e) {
                ((ExecuteAndSwallowException) () -> perunAdapter.removeMemberFromGroup(
                        adminsGroupAttribute.valueAsLong(), memberId)).execute(log);
                return false;
            }
        }

        return true;
    }

    @Override
    public void rejectAddAdmin(@NonNull User user, @NonNull String code)
            throws ExpiredCodeException, InternalErrorException
    {
        LinkCode linkCode = linkCodeManager.get(code);
        if (linkCode == null) {
            log.error("User trying to become reject becoming with invalid code: {}", code);
            throw new ExpiredCodeException("Code is invalid");
        }

        linkCodeManager.delete(code);
    }

    @Override
    public LinkCode getCodeByString(@NonNull String code) {
        return linkCodeManager.get(code);
    }

    @Override
    public Facility getFacilityDetails(@NonNull Long facilityId, @NonNull User user)
            throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException, InternalErrorException,
            UnauthorizedActionException, PerunUnknownException, PerunConnectionException
    {
        Facility facility = facilitiesService.getFacility(facilityId, user.getId(), false, false);
        if (facility != null) {
            facility.getAttributes().get(AttributeCategory.PROTOCOL).clear();
            facility.getAttributes().get(AttributeCategory.ACCESS_CONTROL).clear();
        }

        return facility;
    }

    // private methods

    private Map<String, String> generateCodesForAdmins(List<String> admins, User user,
                                                       Long facility)
            throws InternalErrorException
    {
        List<LinkCode> codes = new ArrayList<>();
        Map<String, String> adminCodesMap = new HashMap<>();

        for (String admin : admins) {
            LinkCode code = this.createAddAdminCode(admin, user, facility);
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
        Map<String, String> linksMap = new HashMap<>();

        for (Map.Entry<String, String> entry : adminCodeMap.entrySet()) {
            String code = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString());
            String link = approvalsProperties.getAdminsEndpoint().concat("?code=").concat(code);
            linksMap.put(entry.getKey(), link);
        }

        return linksMap;
    }

    private LinkCode createAddAdminCode(String admin, User user, Long facility) {
        LinkCode code = new LinkCode();
        code.setRecipientEmail(admin);
        code.setSenderName(user.getName());
        code.setSenderEmail(user.getEmail());
        code.setExpiresAt(approvalsProperties.getConfirmationPeriod().getDays(),
                approvalsProperties.getConfirmationPeriod().getHours());
        code.setFacilityId(facility);
        code.setHash(ServiceUtils.getHash(code.toString()));
        return code;
    }

}
