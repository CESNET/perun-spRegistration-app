package cz.metacentrum.perun.spRegistration.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.metacentrum.perun.spRegistration.Utils;
import cz.metacentrum.perun.spRegistration.common.configs.AppBeansContainer;
import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.configs.ApprovalsProperties;
import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.enums.RequestAction;
import cz.metacentrum.perun.spRegistration.common.enums.RequestStatus;
import cz.metacentrum.perun.spRegistration.common.exceptions.ActiveRequestExistsException;
import cz.metacentrum.perun.spRegistration.common.exceptions.CannotChangeStatusException;
import cz.metacentrum.perun.spRegistration.common.exceptions.ExpiredCodeException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.AttrInput;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.Group;
import cz.metacentrum.perun.spRegistration.common.models.LinkCode;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.Request;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.enums.ServiceEnvironment;
import cz.metacentrum.perun.spRegistration.persistence.enums.ServiceProtocol;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.persistence.managers.LinkCodeManager;
import cz.metacentrum.perun.spRegistration.persistence.managers.ProvidedServiceManager;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestManager;
import cz.metacentrum.perun.spRegistration.persistence.models.ProvidedService;
import cz.metacentrum.perun.spRegistration.service.FacilitiesService;
import cz.metacentrum.perun.spRegistration.service.MailsService;
import cz.metacentrum.perun.spRegistration.service.RequestsService;
import cz.metacentrum.perun.spRegistration.service.ServiceUtils;
import cz.metacentrum.perun.spRegistration.service.UtilsService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cz.metacentrum.perun.spRegistration.service.impl.MailsServiceImpl.LANG_EN;
import static cz.metacentrum.perun.spRegistration.service.impl.MailsServiceImpl.REQUEST_CREATED;
import static cz.metacentrum.perun.spRegistration.service.impl.MailsServiceImpl.REQUEST_MODIFIED;

@Service("requestsService")
@Slf4j
public class RequestsServiceImpl implements RequestsService {

    private static final String UNDEFINED = "_%UNDEFINED%_";

    @NonNull private final PerunAdapter perunAdapter;
    @NonNull private final MailsService mailsService;
    @NonNull private final UtilsService utilsService;
    @NonNull private final RequestManager requestManager;
    @NonNull private final LinkCodeManager linkCodeManager;
    @NonNull private final FacilitiesService facilitiesService;
    @NonNull private final AppBeansContainer applicationBeans;
    @NonNull private final ApplicationProperties applicationProperties;
    @NonNull private final AttributesProperties attributesProperties;
    @NonNull private final ApprovalsProperties approvalsProperties;
    @NonNull private final ProvidedServiceManager providedServiceManager;
    @NonNull private final List<AttrInput> serviceInputs;
    @NonNull private final List<AttrInput> organizationInputs;
    @NonNull private final List<AttrInput> membershipInputs;
    @NonNull private final List<AttrInput> oidcInputs;
    @NonNull private final List<AttrInput> samlInputs;

    @Autowired
    public RequestsServiceImpl(@NonNull PerunAdapter perunAdapter,
                               @NonNull MailsService mailsService,
                               @NonNull UtilsService utilsService,
                               @NonNull RequestManager requestManager,
                               @NonNull LinkCodeManager linkCodeManager,
                               @NonNull FacilitiesService facilitiesService,
                               @NonNull AppBeansContainer applicationBeans,
                               @NonNull ApplicationProperties applicationProperties,
                               @NonNull AttributesProperties attributesProperties,
                               @NonNull ApprovalsProperties approvalsProperties,
                               @NonNull ProvidedServiceManager providedServiceManager,
                               @Qualifier("serviceInputs") List<AttrInput> serviceInputs,
                               @Qualifier("organizationInputs") List<AttrInput> organizationInputs,
                               @Qualifier("membershipInputs") List<AttrInput> membershipInputs,
                               @Qualifier("oidcInputs") List<AttrInput> oidcInputs,
                               @Qualifier("samlInputs") List<AttrInput> samlInputs)
    {
        this.perunAdapter = perunAdapter;
        this.mailsService = mailsService;
        this.utilsService = utilsService;
        this.requestManager = requestManager;
        this.linkCodeManager = linkCodeManager;
        this.facilitiesService = facilitiesService;
        this.applicationBeans = applicationBeans;
        this.applicationProperties = applicationProperties;
        this.attributesProperties = attributesProperties;
        this.approvalsProperties = approvalsProperties;
        this.providedServiceManager = providedServiceManager;
        this.serviceInputs = serviceInputs;
        this.organizationInputs = organizationInputs;
        this.membershipInputs = membershipInputs;
        this.oidcInputs = oidcInputs;
        this.samlInputs = samlInputs;
    }

    @Override
    public Long createRegistrationRequest(Long userId, List<PerunAttribute> attributes) throws InternalErrorException {
        log.trace("createRegistrationRequest(userId: {}, attributes: {})", userId, attributes);

        if (Utils.checkParamsInvalid(userId, attributes)) {
            log.error("Wrong parameters passed: (userId: {}, attributes: {})", userId, attributes);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        Request req = null;
        try {
            req = createRequest(null, userId, RequestAction.REGISTER_NEW_SP, attributes);
        } catch (ActiveRequestExistsException e) {
            //this cannot happen as the registration is for new service and thus facility id will be always null
        }

        if (req == null || req.getReqId() == null) {
            log.error("Could not create request");
            throw new InternalErrorException("Could not create request");
        }

        mailsService.notifyUser(req, REQUEST_CREATED);
        mailsService.notifyAppAdmins(req, REQUEST_CREATED);

        log.trace("createRegistrationRequest returns: {}", req.getReqId());
        return req.getReqId();
    }

    @Override
    public Long createFacilityChangesRequest(@NonNull Long facilityId, @NonNull Long userId,
                                             @NonNull List<PerunAttribute> attributes)
            throws UnauthorizedActionException, InternalErrorException, ActiveRequestExistsException,
            PerunUnknownException, PerunConnectionException
    {
        if (!utilsService.isAdminForFacility(facilityId, userId)) {
            log.error("User is not registered as facility admin, cannot create request");
            throw new UnauthorizedActionException("User is not registered as facility admin, cannot create request");
        }

        Facility facility = perunAdapter.getFacilityById(facilityId);
        if (facility == null) {
            log.error("Could not fetch facility for facilityId: {}", facilityId);
            throw new InternalErrorException("Could not fetch facility for facilityId: " + facilityId);
        }

        boolean attrsChanged = false;
        List<String> attrNames = attributes.stream().map(PerunAttribute::getFullName).collect(Collectors.toList());
        Map<String, PerunAttribute> actualAttrs = perunAdapter.getFacilityAttributes(facilityId, attrNames);
        for (PerunAttribute a: attributes) {
            if (actualAttrs.containsKey(a.getFullName())) {
                PerunAttribute actualA = actualAttrs.get(a.getFullName());
                if (!actualA.equals(a)) {
                    attrsChanged = true;
                    a.setOldValue(actualA.getValue() == null ?
                            JsonNodeFactory.instance.textNode(UNDEFINED) : actualA.getValue());
                }
            }
        }

        if (!attrsChanged) {
            return null;
        }

        Request req = this.createRequest(facilityId, userId, RequestAction.UPDATE_FACILITY, attributes);
        if (req.getReqId() == null) {
            log.error("Could not create request");
            throw new InternalErrorException("Could not create request");
        }

        mailsService.notifyUser(req, REQUEST_CREATED);
        mailsService.notifyAppAdmins(req, REQUEST_CREATED);

        return req.getReqId();
    }

    @Override
    public Long createRemovalRequest(@NonNull Long userId, @NonNull Long facilityId)
            throws UnauthorizedActionException, InternalErrorException, ActiveRequestExistsException,
            PerunUnknownException, PerunConnectionException
    {
        if (!utilsService.isAdminForFacility(facilityId, userId)) {
            log.error("User is not registered as facility admin, cannot create request");
            throw new UnauthorizedActionException("User is not registered as facility admin, cannot create request");
        }

        List<String> attrsToFetch = new ArrayList<>(applicationBeans.getAllAttrNames());
        Map<String, PerunAttribute> attrs = perunAdapter.getFacilityAttributes(facilityId, attrsToFetch);
        boolean isOidc = ServiceUtils.isOidcAttributes(attrs, attributesProperties.getEntityIdAttrName());
        List<String> keptAttrs = this.filterProtocolAttrs(isOidc);
        List<PerunAttribute> facilityAttributes = ServiceUtils.filterFacilityAttrs(attrs, keptAttrs);

        Request req = this.createRequest(facilityId, userId, RequestAction.DELETE_FACILITY, facilityAttributes);
        if (req.getReqId() == null) {
            log.error("Could not create request");
            throw new InternalErrorException("Could not create request");
        }

        mailsService.notifyUser(req, REQUEST_CREATED);
        mailsService.notifyAppAdmins(req, REQUEST_CREATED);

        return req.getReqId();
    }

    @Override
    public Long createMoveToProductionRequest(@NonNull Long facilityId, @NonNull User user,
                                              @NonNull List<String> authorities)
            throws UnauthorizedActionException, InternalErrorException, ActiveRequestExistsException,
            BadPaddingException, InvalidKeyException, IllegalBlockSizeException, UnsupportedEncodingException,
            PerunUnknownException, PerunConnectionException
    {
        if (!utilsService.isAdminForFacility(facilityId, user.getId())) {
            log.error("User is not registered as admin for facility, cannot ask for moving to production");
            throw new UnauthorizedActionException("User is not registered as admin for facility, cannot ask for" +
                    " moving to production");
        }

        Facility fac = facilitiesService.getFacility(facilityId, user.getId(), true, false);
        if (fac == null) {
            log.error("Could not retrieve facility for id: {}", facilityId);
            throw new InternalErrorException("Could not retrieve facility for id: " + facilityId);
        }

        List<PerunAttribute> filteredAttributes = this.filterNotNullAttributes(fac);

        Request req = this.createRequest(facilityId, user.getId(), RequestAction.MOVE_TO_PRODUCTION, filteredAttributes);

        Map<String, String> authoritiesCodesMap = this.generateCodesForAuthorities(req, authorities, user);
        Map<String, String> authoritiesLinksMap = this.generateLinksForAuthorities(authoritiesCodesMap);

        mailsService.notifyUser(req, REQUEST_CREATED);
        mailsService.notifyAppAdmins(req, REQUEST_CREATED);
        mailsService.notifyAuthorities(req, authoritiesLinksMap);

        return req.getReqId();
    }

    @Override
    public boolean updateRequest(@NonNull Long requestId, @NonNull Long userId,
                                 @NonNull List<PerunAttribute> attributes)
            throws UnauthorizedActionException, InternalErrorException
    {
        Request request = requestManager.getRequestById(requestId);
        if (request == null) {
            log.error("Could not retrieve request for id: {}", requestId);
            throw new InternalErrorException("Could not retrieve request for id: " + requestId);
        } else if (!utilsService.isAdminForRequest(request.getReqUserId(), userId)) {
            log.error("User is not registered as admin in request, cannot update it");
            throw new UnauthorizedActionException("User is not registered as admin in request, cannot update it");
        }

        request.updateAttributes(attributes, true, applicationBeans);
        request.setStatus(RequestStatus.WAITING_FOR_APPROVAL);
        request.setModifiedBy(userId);
        request.setModifiedAt(new Timestamp(System.currentTimeMillis()));

        boolean requestUpdated = requestManager.updateRequest(request);
        if (!requestUpdated) {
            return false;
        }

        mailsService.notifyUser(request, REQUEST_MODIFIED);
        mailsService.notifyAppAdmins(request, REQUEST_MODIFIED);
        return true;
    }

    @Override
    public Request getRequestForSignatureByCode(@NonNull String code)
            throws ExpiredCodeException, InternalErrorException
    {
        LinkCode linkCode = linkCodeManager.get(code);
        if (linkCode == null) {
            log.error("User trying to get request with expired code: {}", code);
            throw new ExpiredCodeException("Code has expired");
        } else if (linkCode.getRequestId() == null) {
            log.error("User trying to get request with code without request id: {}", linkCode);
            throw new InternalErrorException("Code has no request id");
        }

        Long requestId = linkCode.getRequestId();
        Request request = requestManager.getRequestById(requestId);

        if (request == null) {
            log.error("Cannot find request from code");
            throw new InternalErrorException("Cannot find request from code");
        }

        return request;
    }

    @Override
    public Request getRequest(@NonNull Long requestId, @NonNull  Long userId)
            throws UnauthorizedActionException, InternalErrorException, PerunUnknownException,
            PerunConnectionException
    {
        Request request = requestManager.getRequestById(requestId);
        if (request == null) {
            log.error("Could not retrieve request for id: {}", requestId);
            throw new InternalErrorException("Could not retrieve request for id: " + requestId);
        } else if (!applicationProperties.isAppAdmin(userId)
                && !utilsService.isAdminForRequest(request.getReqUserId(), userId)) {
            log.error("User cannot view request, user is not requester nor appAdmin");
            throw new UnauthorizedActionException("User cannot view request, user is not a requester");
        }

        if (request.getReqUserId() != null) {
            try {
                User user = perunConnector.getUserById(request.getReqUserId());
                request.setRequester(user);
                User modifier = perunConnector.getUserById(request.getModifiedBy());
                request.setModifier(modifier);
            } catch (ConnectorException e) {
                log.error("Could not fetch requester or modifier for request {}", requestId);
            }
        }

        return request;
    }

    @Override
    public List<Request> getAllUserRequests(@NonNull Long userId)
            throws PerunUnknownException, PerunConnectionException
    {
        Set<Request> requests = new HashSet<>();

        List<Request> userRequests = requestManager.getAllRequestsByUserId(userId);
        if (userRequests != null && !userRequests.isEmpty()) {
            requests.addAll(userRequests);
        }

        Set<Long> facilityIdsWhereUserIsAdmin = perunAdapter.getFacilityIdsWhereUserIsAdmin(userId);
        if (facilityIdsWhereUserIsAdmin != null && !facilityIdsWhereUserIsAdmin.isEmpty()) {
            List<Request> facilitiesRequests = requestManager.getAllRequestsByFacilityIds(facilityIdsWhereUserIsAdmin);
            if (facilitiesRequests != null && !facilitiesRequests.isEmpty()) {
                requests.addAll(facilitiesRequests);
            }
        }

        return new ArrayList<>(requests);
    }

    @Override
    public List<Request> getAllRequests(@NonNull Long userId) throws UnauthorizedActionException {
       if (!applicationProperties.isAppAdmin(userId)) {
            log.error("User cannot list all requests, user is not an admin");
            throw new UnauthorizedActionException("User cannot list all requests, user is not an admin");
        }

        return requestManager.getAllRequests();
    }

    @Override
    public boolean approveRequest(@NonNull Long requestId, @NonNull Long userId)
            throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException,
            PerunUnknownException, PerunConnectionException
    {
        if (!applicationProperties.isAppAdmin(userId)) {
            log.error("User is not authorized to approve request");
            throw new UnauthorizedActionException("User is not authorized to approve request");
        }

        Request request = requestManager.getRequestById(requestId);
        if (request == null) {
            log.error("Could not fetch request with ID: {} from database", requestId);
            throw new InternalErrorException("Could not fetch request with ID: " + requestId + " from database");
        } else if (!this.hasCorrectStatus(request.getStatus(),
                new RequestStatus[] {RequestStatus.WAITING_FOR_CHANGES, RequestStatus.WAITING_FOR_APPROVAL})) {
            log.error("Cannot approve request, request is not in valid status");
            throw new CannotChangeStatusException("Cannot approve request, request is not in valid status");
        }

        request.setStatus(RequestStatus.APPROVED);
        request.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        request.setModifiedBy(userId);
        request.updateAttributes(new ArrayList<>(), false, applicationBeans);

        boolean requestProcessed = this.processApprovedRequest(request);
        if (!requestProcessed) {
            log.error("Request has not been processed");
            return false;
        }

        boolean requestUpdated = requestManager.updateRequest(request);
        if (!requestUpdated) {
            log.error("Request has not been updated");
        }

        mailsService.notifyUser(request, MailsServiceImpl.REQUEST_STATUS_UPDATED);
        return true;
    }

    @Override
    public boolean rejectRequest(@NonNull Long requestId, @NonNull Long userId)
            throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException
    {
        if (!applicationProperties.isAppAdmin(userId)) {
            log.error("User is not authorized to reject request");
            throw new UnauthorizedActionException("User is not authorized to reject request");
        }

        Request request = requestManager.getRequestById(requestId);
        if (request == null) {
            log.error("Could not fetch request with ID: {} from database", requestId);
            throw new InternalErrorException("Could not fetch request with ID: " + requestId + " from database");
        } else if (!this.hasCorrectStatus(request.getStatus(),
                new RequestStatus[] {RequestStatus.WAITING_FOR_APPROVAL, RequestStatus.WAITING_FOR_CHANGES})) {
            log.error("Cannot reject request, request is not in valid status");
            throw new CannotChangeStatusException("Cannot reject request, request is not in valid status");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        request.setModifiedBy(userId);

        boolean requestUpdated = requestManager.updateRequest(request);

        if (requestUpdated) {
            mailsService.notifyUser(request, MailsServiceImpl.REQUEST_STATUS_UPDATED);
            log.info("Request updated, notification sent");
        } else {
            log.error("some operations failed: requestUpdated: false for request: {}", request);
        }

        return requestUpdated;
    }

    @Override
    public boolean askForChanges(@NonNull Long requestId, @NonNull Long userId,
                                 @NonNull List<PerunAttribute> attributes)
            throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException
    {
        if (!applicationProperties.isAppAdmin(userId)) {
            log.error("User is not authorized to ask for changes");
            throw new UnauthorizedActionException("User is not authorized to ask for changes");
        }

        Request request = requestManager.getRequestById(requestId);
        if (request == null) {
            log.error("Could not fetch request with ID: {} from database", requestId);
            throw new InternalErrorException("Could not fetch request with ID: " + requestId + " from database");
        } else if (!this.hasCorrectStatus(request.getStatus(),
                new RequestStatus[] {RequestStatus.WAITING_FOR_APPROVAL, RequestStatus.WAITING_FOR_CHANGES})) {
            log.error("Cannot ask for changes, request not marked as WFA nor WFC");
            throw new CannotChangeStatusException("Cannot ask for changes, request not marked as WFA nor WFC");
        }

        request.updateAttributes(attributes, false, applicationBeans);
        request.setStatus(RequestStatus.WAITING_FOR_CHANGES);
        request.setModifiedBy(userId);
        request.setModifiedAt(new Timestamp(System.currentTimeMillis()));

        boolean requestUpdated = requestManager.updateRequest(request);

        if (requestUpdated) {
            log.info("Request updated, sending notification");
            mailsService.notifyUser(request, MailsServiceImpl.REQUEST_STATUS_UPDATED);
        } else {
            log.error("some operations failed: request has not been updated: {}", request);
        }

        return requestUpdated;
    }

    private Request createRequest(Long facilityId, @NonNull Long userId,
                                  @NonNull RequestAction action, @NonNull List<PerunAttribute> attributes)
            throws InternalErrorException, ActiveRequestExistsException
    {
        Request request = new Request();
        request.setFacilityId(facilityId);
        request.setStatus(RequestStatus.WAITING_FOR_APPROVAL);
        request.setAction(action);
        request.updateAttributes(attributes, true, applicationBeans);
        request.setReqUserId(userId);
        request.setModifiedBy(userId);
        request.setModifiedAt(new Timestamp(System.currentTimeMillis()));

        Long requestId = requestManager.createRequest(request);
        if (requestId == null) {
            log.error("Could not create request: {} in DB", request);
            throw new InternalErrorException("Could not create request in DB");
        }
        request.setReqId(requestId);
        return request;
    }

    private boolean processApprovedRequest(@NonNull Request request)
            throws InternalErrorException, PerunUnknownException, PerunConnectionException
    {
        switch(request.getAction()) {
            case REGISTER_NEW_SP:
                return registerNewFacilityToPerun(request);
            case UPDATE_FACILITY:
                return updateFacilityInPerun(request);
            case DELETE_FACILITY:
                return deleteFacilityFromPerun(request);
            case MOVE_TO_PRODUCTION:
                return moveToProduction(request);
        }

        return false;
    }

    private boolean registerNewFacilityToPerun(@NonNull Request request) throws InternalErrorException {
        String name = this.getNameFromRequest(request);
        PerunAttribute clientId = this.generateClientIdAttribute();
        String desc = this.getDescFromRequest(request, clientId);

        Facility facility = new Facility(null);
        facility.setPerunName(name);
        facility.setPerunDescription(desc);
        boolean isOidcService = ServiceUtils.isOidcRequest(request, attributesProperties.getEntityIdAttrName());
        try {
            facility = perunAdapter.createFacilityInPerun(facility.toJson());
        } catch (Exception e) {
            throw new InternalErrorException("Creating facility in Perun failed");
        }
        if (facility == null) {
            log.error("Creating facility in Perun failed");
            throw new InternalErrorException("Creating facility in Perun failed");
        }

        request.setFacilityId(facility.getId());

        ProvidedService sp = new ProvidedService();
        sp.setFacilityId(facility.getId());
        sp.setName(request.getFacilityName(attributesProperties.getServiceNameAttrName()));
        sp.setDescription(request.getFacilityDescription(attributesProperties.getServiceDescAttrName()));
        sp.setEnvironment(ServiceEnvironment.TESTING);
        sp.setProtocol(ServiceUtils.isOidcRequest(request, attributesProperties.getEntityIdAttrName()) ?
                ServiceProtocol.OIDC : ServiceProtocol.SAML);
        Long adminsGroupId;
        Group adminsGroup = new Group(null, facility.getPerunName(), facility.getPerunName(),
                "Administrators of SP - " + facility.getPerunName(),
                applicationProperties.getSpAdminsRootGroupId(),
                applicationProperties.getSpAdminsRootVoId());
        try {
            providedServiceManager.create(sp);
            adminsGroup = perunAdapter.createGroup(adminsGroup.getParentGroupId(), adminsGroup);
            adminsGroupId = adminsGroup.getId();
            boolean adminSet = perunAdapter.addGroupAsAdmins(facility.getId(), adminsGroupId);
            if (adminSet) {
                Long memberId = perunAdapter.getMemberIdByUser(applicationProperties.getSpAdminsRootVoId(),
                        request.getReqUserId());
                if (memberId != null) {
                    adminSet = perunAdapter.addMemberToGroup(adminsGroupId, memberId);
                } else {
                    log.error("Could not add requester {} as a member({}) of group {}",
                            request.getReqUserId(), memberId, adminsGroupId);
                }
            } else {
                log.error("Could not set created group {} as managers for facility {}", adminsGroupId, facility.getId());
            }
            PerunAttribute adminsGroupAttr = generateAdminsGroupAttr(adminsGroup.getId());
            PerunAttribute testSp = generateTestSpAttribute(true);
            PerunAttribute showOnServiceList = generateShowOnServiceListAttribute(false);
            PerunAttribute proxyIdentifiers = generateProxyIdentifiersAttribute();
            PerunAttribute masterProxyIdentifiers = generateMasterProxyIdentifierAttribute();
            PerunAttribute authProtocol = generateAuthProtocolAttribute(ServiceUtils.isOidcRequest(request,
                    attributesProperties.getEntityIdAttrName()));

            ArrayNode attributes = request.getAttributesAsJsonArrayForPerun();
            if (ServiceUtils.isOidcRequest(request, attributesProperties.getEntityIdAttrName())) {
                for (int i = 0; i < 10; i++) {
                    try {
                        perunAdapter.setFacilityAttribute(facility.getId(), clientId.toJson());
                        break;
                    } catch (Exception e) {
                        log.warn("Failed to set attribute clientId with value {} for facility {}",
                                clientId.valueAsString(), facility.getId());
                        clientId = generateClientIdAttribute();
                    }
                }

                PerunAttribute clientSecret = utilsService.generateClientSecretAttribute();
                perunAdapter.setFacilityAttribute(facility.getId(), clientSecret.toJson());
            }
            attributes.add(adminsGroupAttr.toJson());
            attributes.add(testSp.toJson());
            attributes.add(showOnServiceList.toJson());
            attributes.add(proxyIdentifiers.toJson());
            attributes.add(masterProxyIdentifiers.toJson());
            attributes.add(authProtocol.toJson());
            boolean attributesSet = perunConnector.setFacilityAttributes(request.getFacilityId(), attributes);

            boolean successful = (adminSet && attributesSet);
            if (!successful) {
                log.error("Some operations failed - adminSet: {}, attributesSet: {}", adminSet, attributesSet);
            } else {
                log.info("Facility is all set up");
            }
            log.trace("registerNewFacilityToPerun returns: {}", successful);
            return successful;
        } catch (JsonProcessingException | PerunUnknownException | PerunConnectionException |
                BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            log.error("Caught ConnectorException", e);
            try {
                perunAdapter.deleteFacilityFromPerun(facility.getId());
            } catch (PerunUnknownException | PerunConnectionException ex) {
                log.error("Caught ConnectorException when deleting facility", ex);
            }
            return false;
        }
    }

    private boolean updateFacilityInPerun(@NonNull Request request)
            throws InternalErrorException, PerunUnknownException, PerunConnectionException
    {
        Long facilityId = extractFacilityIdFromRequest(request);

        Facility actualFacility = perunAdapter.getFacilityById(facilityId);
        if (actualFacility == null) {
            log.error("Facility with ID {} does not exist in Perun", facilityId);
            throw new InternalErrorException("Facility with ID " + facilityId + " does not exist in Perun");
        }

        Map<String, PerunAttribute> oldAttributes = perunAdapter.getFacilityAttributes(facilityId,
                request.getAttributeNames());
        if (oldAttributes == null || oldAttributes.isEmpty()) {
            log.error("Could not fetch actual attributes for facility with id {}", facilityId);
            throw new InternalErrorException("Could not fetch actual attributes for facility with id " + facilityId);
        }

        ProvidedService sp = providedServiceManager.getByFacilityId(facilityId);
        Map<String, String> oldName = sp.getName();
        Map<String, String> oldDesc = sp.getDescription();

        try {
            if (!perunAdapter.setFacilityAttributes(request.getFacilityId(),
                    request.getAttributesAsJsonArrayForPerun())) {
                log.error("Some operations failed - attributesSet: false");
            } else {
                log.info("Facility has been updated in Perun");
            }
            sp.setName(request.getFacilityName(attributesProperties.getServiceNameAttrName()));
            sp.setDescription(request.getFacilityDescription(attributesProperties.getServiceDescAttrName()));
            providedServiceManager.update(sp);
        } catch (JsonProcessingException e) {
            log.warn("Caught ConnectorException", e);

            log.trace("updateFacilityInPerun returns: {}", perunAdapter.setFacilityAttributes(request.getFacilityId(),
                    request.getAttributesAsJsonArrayForPerun()));
            boolean attrsSet = perunAdapter.setFacilityAttributes(request.getFacilityId(),
                    request.getAttributesAsJsonArrayForPerun());
            if (!attrsSet) {
                throw new InternalErrorException("Could not set new attributes for facility");
            }
            return true;
        } catch (Exception e) {
            try {
                ArrayNode oldAttrsArray = JsonNodeFactory.instance.arrayNode();
                oldAttributes.values().forEach(a -> oldAttrsArray.add(a.toJson()));
                perunAdapter.setFacilityAttributes(actualFacility.getId(), oldAttrsArray);
                sp.setDescription(oldDesc);
                sp.setName(oldName);
                providedServiceManager.update(sp);
            } catch (Exception ex) {
                log.warn("Caught Exception", ex);
            }
            return false;
        }
        return true;
    }

    private boolean deleteFacilityFromPerun(@NonNull Request request)
            throws InternalErrorException, PerunUnknownException, PerunConnectionException
    {
        Long facilityId = this.extractFacilityIdFromRequest(request);

        PerunAttribute adminsGroupAttr = perunAdapter.getFacilityAttribute(request.getFacilityId(),
                attributesProperties.getManagerGroupAttrName());
        if (adminsGroupAttr == null || adminsGroupAttr.valueAsLong() == null) {
            log.warn("No admins group ID found for facility: {}", facilityId);
        } else {
            Long groupId = adminsGroupAttr.valueAsLong();
            boolean removedGroupFromAdmins = perunAdapter.removeGroupFromAdmins(request.getFacilityId(), groupId);
            if (removedGroupFromAdmins) {
                perunAdapter.deleteGroup(groupId);
            }
        }

        boolean facilityRemoved = perunAdapter.deleteFacilityFromPerun(facilityId);
        providedServiceManager.deleteByFacilityId(facilityId);

        if (facilityRemoved) {
            log.info("Facility has been deleted");
        } else {
            log.error("Facility has not been deleted");
        }

        return facilityRemoved;
    }

    private boolean setClientIdAttribute(@NonNull Long facilityId, @NonNull PerunAttribute clientId) {
        for (int i = 0; i < 20; i++) {
            try {
                boolean set = perunAdapter.setFacilityAttribute(facilityId, clientId.toJson());
                if (set) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("Failed to set attribute clientId with value {} for facility {}",
                        clientId.valueAsString(), facilityId);
                clientId = this.generateClientIdAttribute();
            }
        }
        return false;
    }

    private ArrayNode generateAttributesJsonArray(@NonNull ArrayNode attributes, boolean isOidcService)
            throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException
    {
        if (isOidcService) {
            PerunAttribute clientSecret = utilsService.generateClientSecretAttribute();
            attributes.add(clientSecret.toJson());
        }

        attributes.add(this.generateTestSpAttribute(true).toJson());
        attributes.add(this.generateShowOnServiceListAttribute(false).toJson());
        attributes.add(this.generateProxyIdentifiersAttribute().toJson());
        attributes.add(this.generateMasterProxyIdentifierAttribute().toJson());
        attributes.add(this.generateAuthProtocolAttribute(isOidcService).toJson());

        return attributes;
    }

    private boolean moveToProduction(@NonNull Request request)
            throws PerunUnknownException, PerunConnectionException
    {
        PerunAttribute testSp = this.generateTestSpAttribute(false);
        PerunAttribute showOnServiceList = this.generateShowOnServiceListAttribute(true);

        ArrayNode attributes = request.getAttributesAsJsonArrayForPerun();
        attributes.add(testSp.toJson());
        attributes.add(showOnServiceList.toJson());

        ProvidedService sp = providedServiceManager.getByFacilityId(request.getFacilityId());
        sp.setEnvironment(ServiceEnvironment.PRODUCTION);
        try {
            providedServiceManager.update(sp);
        } catch (InternalErrorException | JsonProcessingException ex) {
            log.warn("Caught Exception when updating ProvidedService {}", sp, ex);
        }

        return perunAdapter.setFacilityAttributes(request.getFacilityId(), attributes);
    }

    private String getDescFromRequest(@NonNull Request request, @NonNull PerunAttribute clientId) {
        if (ServiceUtils.isOidcRequest(request, attributesProperties.getEntityIdAttrName())) {
            return clientId.valueAsString();
        } else {
            return request.getAttributes().get(AttributeCategory.PROTOCOL)
                    .get(attributesProperties.getEntityIdAttrName()).valueAsString();
        }
    }

    private String getNameFromRequest(@NonNull Request request) throws InternalErrorException {
        Map<String, String> nameAttrValue = request.getFacilityName(attributesProperties.getServiceNameAttrName());
        if (nameAttrValue.isEmpty() || !nameAttrValue.containsKey(LANG_EN) || nameAttrValue.get(LANG_EN) == null) {
            throw new InternalErrorException("No name could be found");
        }

        String newName = nameAttrValue.get(LANG_EN);
        Pattern pattern = Pattern.compile("[^A-Za-z0-9]");
        Pattern pattern2 = Pattern.compile("_+_");

        newName = Normalizer.normalize(newName, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        newName = pattern.matcher(newName).replaceAll("_");
        return pattern2.matcher(newName).replaceAll("_");
    }

    private Long extractFacilityIdFromRequest(@NonNull Request request) throws InternalErrorException {
        Long facilityId = request.getFacilityId();
        if (facilityId == null) {
            log.error("Request: {} does not have facilityId", request);
            throw new InternalErrorException(Utils.GENERIC_ERROR_MSG);
        }
        return facilityId;
    }

    private PerunAttribute generateAuthProtocolAttribute(boolean isOidc) {
        PerunAttribute attribute = new PerunAttribute();
        if (isOidc) {
            attribute.setDefinition(applicationBeans.getAttrDefinition(attributesProperties.getIsOidcAttrName()));
        } else {
            attribute.setDefinition(applicationBeans.getAttrDefinition(attributesProperties.getIsSamlAttrName()));
        }
        attribute.setValue(JsonNodeFactory.instance.booleanNode(true));
        return attribute;
    }

    private PerunAttribute generateMasterProxyIdentifierAttribute() {
       PerunAttribute attribute = new PerunAttribute();
        attribute.setDefinition(applicationBeans.getAttrDefinition(attributesProperties.getMasterProxyIdentifierAttrName()));
        attribute.setValue(JsonNodeFactory.instance.textNode(
                attributesProperties.getMasterProxyIdentifierAttrValue()));
        return attribute;
    }

    private PerunAttribute generateProxyIdentifiersAttribute() {
        PerunAttribute attribute = new PerunAttribute();
        attribute.setDefinition(applicationBeans.getAttrDefinition(attributesProperties.getProxyIdentifierAttrName()));
        ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
        arrayNode.add(attributesProperties.getProxyIdentifierAttrValue());
        attribute.setValue(arrayNode);
        return attribute;
    }

    private PerunAttribute generateShowOnServiceListAttribute(boolean value) {
        PerunAttribute attribute = new PerunAttribute();
        attribute.setDefinition(applicationBeans.getAttrDefinition(attributesProperties.getShowOnServiceListAttrName()));
        attribute.setValue(JsonNodeFactory.instance.booleanNode(value));
        return attribute;
    }

    private PerunAttribute generateTestSpAttribute(boolean value) {
        PerunAttribute attribute = new PerunAttribute();
        attribute.setDefinition(applicationBeans.getAttrDefinition(attributesProperties.getIsTestSpAttrName()));
        attribute.setValue(JsonNodeFactory.instance.booleanNode(value));
        return attribute;
    }

    private PerunAttribute generateClientIdAttribute() {
        PerunAttribute attribute = new PerunAttribute();
        attribute.setDefinition(applicationBeans.getAttrDefinition(attributesProperties.getOidcClientIdAttrName()));

        String clientId = ServiceUtils.generateClientId();
        attribute.setValue(JsonNodeFactory.instance.textNode(clientId));
        return attribute;
    }

    private PerunAttribute generateAdminsGroupAttr(Long id) {
        log.trace("generateAdminsGroupAttr({})", id);

        PerunAttribute attribute = new PerunAttribute();
        attribute.setDefinition(applicationBeans.getAttrDefinition(attributesProperties.getManagerGroupAttrName()));
        attribute.setValue(JsonNodeFactory.instance.numberNode(id));

        log.trace("generateAdminsGroupAttr({}) returns: {}", id, attribute);
        return attribute;
    }

    private List<PerunAttribute> filterNotNullAttributes(@NonNull Facility facility) {
        if (facility.getAttributes() == null || facility.getAttributes().isEmpty()) {
            return new LinkedList<>();
        }
        final List<PerunAttribute> filteredAttributes = new LinkedList<>();
        facility.getAttributes()
                .values()
                .forEach(attrsInCategory -> {
                            if (attrsInCategory != null && !attrsInCategory.isEmpty()) {
                                attrsInCategory.values()
                                        .stream()
                                        .filter(attr -> attr.getValue() != null)
                                        .forEach(filteredAttributes::add);
                            }
                        }
                );

        return filteredAttributes;
    }

    private Map<String, String> generateCodesForAuthorities(@NonNull Request request,
                                                            @NonNull List<String> authorities,
                                                            @NonNull User user)
            throws InternalErrorException
    {
        List<String> emails = new ArrayList<>();
        if (authorities == null || authorities.isEmpty()) {
            emails = approvalsProperties.getDefaultAuthorities();
        } else {
            Map<String, List<String>> authsMap = approvalsProperties.getTransferAuthoritiesMap();
            for (String authoritiesInput: authorities) {
                if (authsMap.containsKey(authoritiesInput)) {
                    emails.addAll(authsMap.get(authoritiesInput));
                }
            }
        }

        List<LinkCode> codes = new LinkedList<>();
        Map<String, String> authsCodesMap = new HashMap<>();

        for (String authority : emails) {
            LinkCode code = this.createRequestCode(authority, user, request.getReqId(), request.getFacilityId());
            codes.add(code);
            authsCodesMap.put(authority, code.getHash());
        }

        linkCodeManager.createMultiple(codes);
        return authsCodesMap;
    }

    private LinkCode createRequestCode(@NonNull String authority, @NonNull User user,
                                       @NonNull Long requestId, @NonNull Long facilityId)
    {
        LinkCode code = new LinkCode();
        code.setRecipientEmail(authority);
        code.setSenderName(user.getName());
        code.setSenderEmail(user.getEmail());
        code.setExpiresAt(approvalsProperties.getConfirmationPeriodDays(),
                approvalsProperties.getConfirmationPeriodHours());
        code.setFacilityId(facilityId);
        code.setRequestId(requestId);
        code.setHash(ServiceUtils.getHash(code.toString()));
        return code;
    }

    private Map<String, String> generateLinksForAuthorities(@NonNull Map<String, String> authorityCodeMap)
            throws UnsupportedEncodingException
    {
        Map<String, String> linksMap = new HashMap<>();
        for (Map.Entry<String, String> entry : authorityCodeMap.entrySet()) {
            String code = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString());
            String link = approvalsProperties.getAuthoritiesEndpoint()
                    .concat("?code=").concat(code);
            linksMap.put(entry.getKey(), link);
        }
        return linksMap;
    }

    private List<String> filterProtocolAttrs(boolean isOidc) {
        List<String> keptAttrs = new ArrayList<>();

        keptAttrs.addAll(serviceInputs.stream()
                .map(AttrInput::getName)
                .collect(Collectors.toList()));

        keptAttrs.addAll(organizationInputs.stream()
                .map(AttrInput::getName)
                .collect(Collectors.toList()));

        keptAttrs.addAll(membershipInputs.stream()
                .map(AttrInput::getName)
                .collect(Collectors.toList()));

        if (isOidc) {
            keptAttrs.addAll(oidcInputs.stream()
                    .map(AttrInput::getName)
                    .collect(Collectors.toList())
            );
            keptAttrs.add(attributesProperties.getOidcClientIdAttrName());
            keptAttrs.add(attributesProperties.getOidcClientSecretAttrName());
        } else {
            keptAttrs.addAll(samlInputs.stream()
                    .map(AttrInput::getName)
                    .collect(Collectors.toList())
            );
        }

        return keptAttrs;
    }

    private boolean hasCorrectStatus(@NonNull RequestStatus status, @NonNull RequestStatus[] allowedStatuses) {
        for (RequestStatus s: allowedStatuses) {
            if (s.equals(status)) {
                return true;
            }
        }

        return false;
    }

}
