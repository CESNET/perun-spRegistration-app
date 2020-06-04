package cz.metacentrum.perun.spRegistration.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.metacentrum.perun.spRegistration.Utils;
import cz.metacentrum.perun.spRegistration.common.configs.AppConfig;
import cz.metacentrum.perun.spRegistration.common.configs.Config;
import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.enums.RequestAction;
import cz.metacentrum.perun.spRegistration.common.enums.RequestStatus;
import cz.metacentrum.perun.spRegistration.common.exceptions.ActiveRequestExistsException;
import cz.metacentrum.perun.spRegistration.common.exceptions.BadRequestException;
import cz.metacentrum.perun.spRegistration.common.exceptions.CannotChangeStatusException;
import cz.metacentrum.perun.spRegistration.common.exceptions.ConnectorException;
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
import cz.metacentrum.perun.spRegistration.persistence.connectors.PerunConnector;
import cz.metacentrum.perun.spRegistration.persistence.enums.ServiceEnvironment;
import cz.metacentrum.perun.spRegistration.persistence.enums.ServiceProtocol;
import cz.metacentrum.perun.spRegistration.persistence.managers.LinkCodeManager;
import cz.metacentrum.perun.spRegistration.persistence.managers.ProvidedServiceManager;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestManager;
import cz.metacentrum.perun.spRegistration.persistence.models.ProvidedService;
import cz.metacentrum.perun.spRegistration.service.FacilitiesService;
import cz.metacentrum.perun.spRegistration.service.MailsService;
import cz.metacentrum.perun.spRegistration.service.RequestsService;
import cz.metacentrum.perun.spRegistration.service.ServiceUtils;
import cz.metacentrum.perun.spRegistration.service.UtilsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cz.metacentrum.perun.spRegistration.service.impl.MailsServiceImpl.REQUEST_CREATED;
import static cz.metacentrum.perun.spRegistration.service.impl.MailsServiceImpl.REQUEST_MODIFIED;

@Service("requestsService")
public class RequestsServiceImpl implements RequestsService {

    private static final Logger log = LoggerFactory.getLogger(RequestsServiceImpl.class);

    private final PerunConnector perunConnector;
    private final AppConfig appConfig;
    private final MailsService mailsService;
    private final UtilsService utilsService;
    private final RequestManager requestManager;
    private final LinkCodeManager linkCodeManager;
    private final FacilitiesService facilitiesService;
    private final Config config;
    private final ProvidedServiceManager providedServiceManager;

    @Value("#{'${mail.approval.default-authorities.mails}'.split(',')}")
    private List<String> defaultAuthorities;

    @Autowired
    public RequestsServiceImpl(PerunConnector perunConnector, AppConfig appConfig, MailsService mailsService,
                               UtilsService utilsService, RequestManager requestManager, LinkCodeManager linkCodeManager,
                               FacilitiesService facilitiesService, Config config, ProvidedServiceManager providedServiceManager) {
        this.perunConnector = perunConnector;
        this.appConfig = appConfig;
        this.mailsService = mailsService;
        this.utilsService = utilsService;
        this.requestManager = requestManager;
        this.linkCodeManager = linkCodeManager;
        this.facilitiesService = facilitiesService;
        this.config = config;
        this.providedServiceManager = providedServiceManager;
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
    public Long createFacilityChangesRequest(Long facilityId, Long userId, List<PerunAttribute> attributes)
            throws UnauthorizedActionException, ConnectorException, InternalErrorException, ActiveRequestExistsException
    {
        log.trace("createFacilityChangesRequest(facility: {}, userId: {}, attributes: {})", facilityId, userId, attributes);

        if (Utils.checkParamsInvalid(facilityId, userId, attributes)) {
            log.error("Wrong parameters passed: (facilityId: {}, userId: {}, attributes: {})",
                    facilityId, userId, attributes);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        } else if (!utilsService.isFacilityAdmin(facilityId, userId)) {
            log.error("User is not registered as facility admin, cannot create request");
            throw new UnauthorizedActionException("User is not registered as facility admin, cannot create request");
        }

        Facility facility = perunConnector.getFacilityById(facilityId);
        if (facility == null) {
            log.error("Could not fetch facility for facilityId: {}", facilityId);
            throw new InternalErrorException("Could not fetch facility for facilityId: " + facilityId);
        }

        boolean attrsChanged = false;
        List<String> attrNames = attributes.stream().map(PerunAttribute::getFullName).collect(Collectors.toList());
        Map<String, PerunAttribute> actualAttrs = perunConnector.getFacilityAttributes(facilityId, attrNames);
        for (PerunAttribute a: attributes) {
            if (actualAttrs.containsKey(a.getFullName())) {
                PerunAttribute actualA = actualAttrs.get(a.getFullName());
                if (!actualA.equals(a)) {
                    attrsChanged = true;
                    a.setOldValue(actualA.getValue() == null ? "_%UNDEFINED%_" : actualA.getValue());
                }
            }
        }

        if (!attrsChanged) {
            return null;
        }


        Request req = createRequest(facilityId, userId, RequestAction.UPDATE_FACILITY, attributes);
        if (req.getReqId() == null) {
            log.error("Could not create request");
            throw new InternalErrorException("Could not create request");
        }

        mailsService.notifyUser(req, REQUEST_CREATED);
        mailsService.notifyAppAdmins(req, REQUEST_CREATED);

        log.trace("createFacilityChangesRequest returns: {}", req.getReqId());
        return req.getReqId();
    }

    @Override
    public Long createRemovalRequest(Long userId, Long facilityId)
            throws UnauthorizedActionException, ConnectorException, InternalErrorException, ActiveRequestExistsException
    {
        log.trace("createRemovalRequest(userId: {}, facilityId: {})", userId, facilityId);

        if (Utils.checkParamsInvalid(userId, facilityId)) {
            log.error("Wrong parameters passed: (facilityId: {}, userId: {})", facilityId, userId);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        } else if (!utilsService.isFacilityAdmin(facilityId, userId)) {
            log.error("User is not registered as facility admin, cannot create request");
            throw new UnauthorizedActionException("User is not registered as facility admin, cannot create request");
        }

        List<String> attrsToFetch = new ArrayList<>(appConfig.getPerunAttributeDefinitionsMap().keySet());
        Map<String, PerunAttribute> attrs = perunConnector.getFacilityAttributes(facilityId, attrsToFetch);
        boolean isOidc = ServiceUtils.isOidcAttributes(attrs, appConfig.getEntityIdAttribute());
        List<String> keptAttrs = getAttrsToKeep(isOidc);
        List<PerunAttribute> facilityAttributes = ServiceUtils.filterFacilityAttrs(attrs, keptAttrs);

        Request req = createRequest(facilityId, userId, RequestAction.DELETE_FACILITY, facilityAttributes);
        if (req.getReqId() == null) {
            log.error("Could not create request");
            throw new InternalErrorException("Could not create request");
        }

        mailsService.notifyUser(req, REQUEST_CREATED);
        mailsService.notifyAppAdmins(req, REQUEST_CREATED);

        log.trace("createRemovalRequest returns: {}", req.getReqId());
        return req.getReqId();
    }

    @Override
    public Long createMoveToProductionRequest(Long facilityId, User user, List<String> authorities)
            throws UnauthorizedActionException, InternalErrorException, ConnectorException, ActiveRequestExistsException,
            BadPaddingException, InvalidKeyException, IllegalBlockSizeException, UnsupportedEncodingException
    {
        log.trace("requestMoveToProduction(facilityId: {}, userId: {}, authorities: {})", facilityId, user, authorities);

        if (Utils.checkParamsInvalid(facilityId, user)) {
            log.error("Wrong parameters passed: (facilityId: {}, userId: {})", facilityId, user);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        } else if (!utilsService.isFacilityAdmin(facilityId, user.getId())) {
            log.error("User is not registered as admin for facility, cannot ask for moving to production");
            throw new UnauthorizedActionException("User is not registered as admin for facility, cannot ask for moving to production");
        }

        Facility fac = facilitiesService.getFacility(facilityId, user.getId(), true, false);
        if (fac == null) {
            log.error("Could not retrieve facility for id: {}", facilityId);
            throw new InternalErrorException("Could not retrieve facility for id: " + facilityId);
        }

        List<PerunAttribute> filteredAttributes = filterNotNullAttributes(fac);

        Request req = createRequest(facilityId, user.getId(), RequestAction.MOVE_TO_PRODUCTION, filteredAttributes);

        Map<String, String> authoritiesCodesMap = generateCodesForAuthorities(req, authorities, user);
        Map<String, String> authoritiesLinksMap = generateLinksForAuthorities(authoritiesCodesMap);

        mailsService.notifyUser(req, REQUEST_CREATED);
        mailsService.notifyAppAdmins(req, REQUEST_CREATED);
        mailsService.notifyAuthorities(req, authoritiesLinksMap);

        log.trace("requestMoveToProduction returns: {}", req.getReqId());
        return req.getReqId();
    }

    @Override
    public boolean updateRequest(Long requestId, Long userId, List<PerunAttribute> attributes)
            throws UnauthorizedActionException, InternalErrorException {
        log.trace("updateRequest(requestId: {}, userId: {}, attributes: {})", requestId, userId, attributes);
        if (requestId == null || userId == null || attributes == null) {
            log.error("Illegal input - requestId: {}, userId: {}, attributes: {}", requestId, userId, attributes);
            throw new IllegalArgumentException("Illegal input - requestId: " + requestId + ", userId: " + userId +
                    ", attributes: " + attributes);
        }

        Request request = requestManager.getRequestById(requestId);
        if (request == null) {
            log.error("Could not retrieve request for id: {}", requestId);
            throw new InternalErrorException("Could not retrieve request for id: " + requestId);
        } else if (!utilsService.isAdminInRequest(request.getReqUserId(), userId)) {
            log.error("User is not registered as admin in request, cannot update it");
            throw new UnauthorizedActionException("User is not registered as admin in request, cannot update it");
        }

        log.info("Updating request");
        request.updateAttributes(attributes, true, appConfig);

        request.setStatus(RequestStatus.WAITING_FOR_APPROVAL);
        request.setModifiedBy(userId);
        request.setModifiedAt(new Timestamp(System.currentTimeMillis()));

        boolean requestUpdated = requestManager.updateRequest(request);

        mailsService.notifyUser(request, REQUEST_MODIFIED);
        mailsService.notifyAppAdmins(request, REQUEST_MODIFIED);

        log.trace("updateRequest returns: {}", requestUpdated);
        return requestUpdated;
    }

    @Override
    public Request getRequestForSignatureByCode(String code) throws ExpiredCodeException, InternalErrorException {
        log.trace("getRequestDetailsForSignature({})", code);
        if (Utils.checkParamsInvalid(code)) {
            log.error("Wrong parameters passed: (code: {})", code);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        LinkCode linkCode = linkCodeManager.get(code);
        if (linkCode == null) {
            log.error("User trying to get request with expired code: {}", code);
            throw new ExpiredCodeException("Code has expired");
        } else if (linkCode.getRequestId() == null) {
            log.error("User trying to get request with code without request id: {}", linkCode);
            throw new InternalErrorException("Code has no request id");
        }

        Long requestId = linkCode.getRequestId();
        log.debug("Fetching request for id: {}", requestId);
        Request request = requestManager.getRequestById(requestId);

        if (request == null) {
            log.error("Cannot find request from code");
            throw new InternalErrorException("Cannot find request from code");
        }

        log.trace("getRequestDetailsForSignature returns: {}", request);
        return request;
    }

    @Override
    public Request getRequest(Long requestId, Long userId)
            throws UnauthorizedActionException, InternalErrorException
    {
        log.trace("getDetailedRequest(requestId: {}, userId: {})", requestId, userId);

        if (Utils.checkParamsInvalid(requestId, userId)) {
            log.error("Wrong parameters passed: (requestId: {}, userId: {})", requestId, userId);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        Request request = requestManager.getRequestById(requestId);
        if (request == null) {
            log.error("Could not retrieve request for id: {}", requestId);
            throw new InternalErrorException("Could not retrieve request for id: " + requestId);
        } else if (!appConfig.isAppAdmin(userId) && !utilsService.isAdminInRequest(request.getReqUserId(), userId)) {
            log.error("User cannot view request, user is not requester nor appAdmin");
            throw new UnauthorizedActionException("User cannot view request, user is not a requester");
        }

        if (request.getReqUserId() != null) {
            try {
                User user = perunConnector.getUserById(request.getReqUserId());
                request.setUser(user);
            } catch (ConnectorException e) {
                log.error("Could not fetch user {} for request {}", request.getReqUserId(), requestId);
            }
        }

        log.trace("getDetailedRequest returns: {}", request);
        return request;
    }

    @Override
    public List<Request> getAllUserRequests(Long userId) throws ConnectorException {
        log.trace("getAllRequestsUserCanAccess({})", userId);

        if (Utils.checkParamsInvalid(userId)) {
            log.error("Wrong parameters passed: (userId: {})", userId);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        Set<Request> requests = new HashSet<>();

        List<Request> userRequests = requestManager.getAllRequestsByUserId(userId);
        if (userRequests != null && !userRequests.isEmpty()) {
            requests.addAll(userRequests);
        }

        Set<Long> facilityIdsWhereUserIsAdmin = perunConnector.getFacilityIdsWhereUserIsAdmin(userId);
        if (facilityIdsWhereUserIsAdmin != null && !facilityIdsWhereUserIsAdmin.isEmpty()) {
            List<Request> facilitiesRequests = requestManager.getAllRequestsByFacilityIds(facilityIdsWhereUserIsAdmin);
            if (facilitiesRequests != null && !facilitiesRequests.isEmpty()) {
                requests.addAll(facilitiesRequests);
            }
        }

        List<Request> unique = new ArrayList<>(requests);
        log.trace("getAllRequestsUserCanAccess returns: {}", unique);
        return unique;
    }

    @Override
    public List<Request> getAllRequests(Long userId) throws UnauthorizedActionException {
        log.trace("getAllRequests({})", userId);

        if (Utils.checkParamsInvalid(userId)) {
            log.error("Wrong parameters passed: (userId: {})", userId);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        } else if (! appConfig.isAppAdmin(userId)) {
            log.error("User cannot list all requests, user is not an admin");
            throw new UnauthorizedActionException("User cannot list all requests, user is not an admin");
        }

        List<Request> result = requestManager.getAllRequests();

        log.trace("getAllRequests returns: {}", result);
        return result;
    }

    @Override
    public boolean approveRequest(Long requestId, Long userId)
            throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException, ConnectorException,
            BadPaddingException, InvalidKeyException, IllegalBlockSizeException, BadRequestException {
        log.trace("approveRequest(requestId: {}, userId: {})", requestId, userId);

        if (Utils.checkParamsInvalid(requestId, userId)) {
            log.error("Wrong parameters passed: (requestId: {}, userId: {})", requestId, userId);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        } else if (! appConfig.isAppAdmin(userId)) {
            log.error("User is not authorized to approve request");
            throw new UnauthorizedActionException("User is not authorized to approve request");
        }

        Request request = requestManager.getRequestById(requestId);
        if (request == null) {
            log.error("Could not fetch request with ID: {} from database", requestId);
            throw new InternalErrorException("Could not fetch request with ID: " + requestId + " from database");
        } else if (!RequestStatus.WAITING_FOR_APPROVAL.equals(request.getStatus())
                && !RequestStatus.WAITING_FOR_CHANGES.equals(request.getStatus())) {
            log.error("Cannot approve request, request is not in valid status");
            throw new CannotChangeStatusException("Cannot approve request, request is not in valid status");
        }

        request.setStatus(RequestStatus.APPROVED);
        request.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        request.setModifiedBy(userId);
        request.updateAttributes(new ArrayList<>(), false, appConfig);

        boolean requestProcessed = processApprovedRequest(request);
        boolean requestUpdated = requestManager.updateRequest(request);
        mailsService.notifyUser(request, MailsServiceImpl.REQUEST_STATUS_UPDATED);

        boolean successful = (requestProcessed && requestUpdated);

        if (!successful) {
            log.error("some operations failed: requestProcessed: {}, requestUpdated: {} for request: {}",
                    requestProcessed, requestUpdated, request);
        } else {
            log.info("Request processed, request updated, notification sent");
        }

        log.trace("approveRequest() returns: {}", successful);
        return successful;
    }

    @Override
    public boolean rejectRequest(Long requestId, Long userId)
            throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException
    {
        log.trace("rejectRequest(requestId: {}, userId: {})", requestId, userId);

        if (Utils.checkParamsInvalid(requestId, userId)) {
            log.error("Wrong parameters passed: (requestId: {}, userId: {})", requestId, userId);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        } else if (! appConfig.isAppAdmin(userId)) {
            log.error("User is not authorized to reject request");
            throw new UnauthorizedActionException("User is not authorized to reject request");
        }

        Request request = requestManager.getRequestById(requestId);
        if (request == null) {
            log.error("Could not fetch request with ID: {} from database", requestId);
            throw new InternalErrorException("Could not fetch request with ID: " + requestId + " from database");
        } else if (!RequestStatus.WAITING_FOR_APPROVAL.equals(request.getStatus())
                && !RequestStatus.WAITING_FOR_CHANGES.equals(request.getStatus())) {
            log.error("Cannot reject request, request is not in valid status");
            throw new CannotChangeStatusException("Cannot reject request, request is not in valid status");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        request.setModifiedBy(userId);

        boolean requestUpdated = requestManager.updateRequest(request);
        mailsService.notifyUser(request, MailsServiceImpl.REQUEST_STATUS_UPDATED);

        if (! requestUpdated) {
            log.error("some operations failed: requestUpdated: false for request: {}", request);
        } else {
            log.info("Request updated, notification sent");
        }

        log.trace("rejectRequest() returns: {}", request);
        return requestUpdated;
    }

    @Override
    public boolean askForChanges(Long requestId, Long userId, List<PerunAttribute> attributes)
            throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException
    {
        log.trace("askForChanges(requestId: {}, userId: {}, attributes: {})", requestId, userId, attributes);

        if (Utils.checkParamsInvalid(requestId, userId)) {
            log.error("Wrong parameters passed: (requestId: {}, userId: {})", requestId, userId);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        } else if (! appConfig.isAppAdmin(userId)) {
            log.error("User is not authorized to ask for changes");
            throw new UnauthorizedActionException("User is not authorized to ask for changes");
        }

        Request request = requestManager.getRequestById(requestId);
        if (request == null) {
            log.error("Could not fetch request with ID: {} from database", requestId);
            throw new InternalErrorException("Could not fetch request with ID: " + requestId + " from database");
        } else if (!RequestStatus.WAITING_FOR_APPROVAL.equals(request.getStatus()) &&
                !RequestStatus.WAITING_FOR_CHANGES.equals(request.getStatus())) {
            log.error("Cannot ask for changes, request not marked as WFA nor WFC");
            throw new CannotChangeStatusException("Cannot ask for changes, request not marked as WFA nor WFC");
        }

        request.updateAttributes(attributes, false, appConfig);
        request.setStatus(RequestStatus.WAITING_FOR_CHANGES);
        request.setModifiedBy(userId);
        request.setModifiedAt(new Timestamp(System.currentTimeMillis()));

        boolean requestUpdated = requestManager.updateRequest(request);
        mailsService.notifyUser(request, MailsServiceImpl.REQUEST_STATUS_UPDATED);

        if (! requestUpdated) {
            log.error("some operations failed: requestUpdated: false for request: {}", request);
        } else {
            log.info("Request updated, notification sent");
        }

        log.trace("askForChanges() returns: {}", requestUpdated);
        return requestUpdated;
    }

    private Request createRequest(Long facilityId, Long userId, RequestAction action, List<PerunAttribute> attributes)
            throws InternalErrorException, ActiveRequestExistsException
    {
        log.trace("createRequest(facilityId: {}, userId: {}, action: {}, attributes: {})",
                facilityId, userId, action, attributes);

        if (Utils.checkParamsInvalid(userId, action, attributes)) {
            log.error("Wrong parameters passed: (userId: {}, action: {}, attributes: {})", userId, action, attributes);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        Request request = new Request();
        request.setFacilityId(facilityId);
        request.setStatus(RequestStatus.WAITING_FOR_APPROVAL);
        request.setAction(action);
        request.updateAttributes(attributes, true, appConfig);
        request.setReqUserId(userId);
        request.setModifiedBy(userId);
        request.setModifiedAt(new Timestamp(System.currentTimeMillis()));

        log.debug("creating request in DB");
        Long requestId = requestManager.createRequest(request);
        if (requestId == null) {
            log.error("Could not create request: {} in DB", request);
            throw new InternalErrorException("Could not create request in DB");
        }

        request.setReqId(requestId);

        log.debug("createRequest returns: {}", request);
        return request;
    }

    private boolean processApprovedRequest(Request request) throws InternalErrorException, ConnectorException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException, BadRequestException {
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

    private boolean registerNewFacilityToPerun(Request request) throws InternalErrorException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException, BadRequestException {
        log.trace("registerNewFacilityToPerun({})", request);

        String newName = request.getFacilityName(appConfig.getServiceNameAttributeName()).get("en");
        Pattern pattern = Pattern.compile("[^A-Za-z0-9]");
        Pattern pattern2 = Pattern.compile("_+_");

        newName = Normalizer.normalize(newName, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        newName = pattern.matcher(newName).replaceAll("_");
        newName = pattern2.matcher(newName).replaceAll("_");
        PerunAttribute clientId = generateClientIdAttribute();
        String newDesc = ServiceUtils.isOidcRequest(request, appConfig.getEntityIdAttribute()) ?
                clientId.valueAsString() : request.getAttributes().get(AttributeCategory.PROTOCOL).get(appConfig.getEntityIdAttribute()).valueAsString();

        if (Utils.checkParamsInvalid(newName)) {
            log.error("Wrong parameters passed: (newName: {})", newName);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        Facility facility = new Facility(null);
        facility.setPerunName(newName);
        facility.setPerunDescription(newDesc);
        try {
            facility = perunConnector.createFacilityInPerun(facility.toJson());
        } catch (ConnectorException e) {
            throw new InternalErrorException("Creating facility in Perun failed");
        }
        if (facility == null) {
            log.error("Creating facility in Perun failed");
            throw new InternalErrorException("Creating facility in Perun failed");
        }

        request.setFacilityId(facility.getId());

        ProvidedService sp = new ProvidedService();
        sp.setFacilityId(facility.getId());
        sp.setName(request.getFacilityName(appConfig.getServiceNameAttributeName()));
        sp.setDescription(request.getFacilityDescription(appConfig.getServiceDescAttributeName()));
        sp.setEnvironment(ServiceEnvironment.TESTING);
        sp.setProtocol(ServiceUtils.isOidcRequest(request, appConfig.getEntityIdAttribute()) ?
                ServiceProtocol.OIDC : ServiceProtocol.SAML);
        Long adminsGroupId = null;
        try {
            providedServiceManager.create(sp);

            Group adminsGroup = new Group(null, facility.getPerunName(), facility.getPerunName(),
                    "Administrators of SP - " + facility.getPerunName(), appConfig.getSpAdminsRootGroupId(),
                    appConfig.getSpAdminsRootVoId());
            adminsGroup = perunConnector.createGroup(adminsGroup.getParentGroupId(), adminsGroup);
            adminsGroupId = adminsGroup.getId();
            boolean adminSet = perunConnector.addGroupAsAdmins(facility.getId(), adminsGroupId);
            if (adminSet) {
                Long memberId = perunConnector.getMemberIdByUser(appConfig.getSpAdminsRootVoId(), request.getReqUserId());
                if (memberId != null) {
                    adminSet = perunConnector.addMemberToGroup(adminsGroupId, memberId);
                } else {
                    log.error("Could not add requester {} as a member({}) of group {}",
                            request.getReqUserId(), memberId, adminsGroupId );
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
                    appConfig.getEntityIdAttribute()));

            ArrayNode attributes = request.getAttributesAsJsonArrayForPerun();
            if (ServiceUtils.isOidcRequest(request, appConfig.getEntityIdAttribute())) {
                for (int i = 0; i < 10; i++) {
                    try {
                        perunConnector.setFacilityAttribute(facility.getId(), clientId.toJson());
                        break;
                    } catch (ConnectorException e) {
                        log.warn("Failed to set attribute clientId with value {} for facility {}",
                                clientId.valueAsString(), facility.getId());
                        clientId = generateClientIdAttribute();
                    }
                }

                PerunAttribute clientSecret = utilsService.generateClientSecretAttribute();
                perunConnector.setFacilityAttribute(facility.getId(), clientSecret.toJson());
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
        } catch (ConnectorException | JsonProcessingException e) {
            log.error("Caught ConnectorException", e);
            try {
                if (adminsGroupId != null) {
                    try {
                        perunConnector.removeGroupFromAdmins(facility.getId(), adminsGroupId);
                    } catch (ConnectorException ex) {
                        log.error("Caught connector exception when removing admin group {} from facility {}. " +
                                "Probably not assigned before", adminsGroupId, facility.getId(), ex);
                    }
                    try {
                        perunConnector.deleteGroup(adminsGroupId);
                    } catch (ConnectorException ex) {
                        log.error("Caught connector exception when removing admin group {}", adminsGroupId, ex);
                    }
                }
                perunConnector.deleteFacilityFromPerun(facility.getId());
            } catch (ConnectorException ex) {
                log.error("Caught ConnectorException when deleting facility", ex);
            }

            return false;
        }
    }

    private boolean updateFacilityInPerun(Request request) throws InternalErrorException, ConnectorException, BadRequestException {
        log.trace("updateFacilityInPerun({})", request);

        if (Utils.checkParamsInvalid(request)) {
            log.error("Wrong parameters passed: (request: {})", request);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        Long facilityId = extractFacilityIdFromRequest(request);

        Facility actualFacility = perunConnector.getFacilityById(facilityId);
        Map<String, PerunAttribute> oldAttributes = perunConnector.getFacilityAttributes(facilityId,
                request.getAttributeNames());

        if (actualFacility == null) {
            log.error("Facility with ID: {} does not exist in Perun", facilityId);
            throw new InternalErrorException("Facility with ID: " + facilityId + " does not exist in Perun");
        }

        ProvidedService sp = providedServiceManager.get(facilityId);
        Map<String, String> oldName = sp.getName();
        Map<String, String> oldDesc = sp.getDescription();

        try {
            if (!perunConnector.setFacilityAttributes(request.getFacilityId(),
                    request.getAttributesAsJsonArrayForPerun())) {
                log.error("Some operations failed - attributesSet: false");
            } else {
                log.info("Facility has been updated in Perun");
            }
            sp = providedServiceManager.get(facilityId);
            sp.setName(request.getFacilityName(appConfig.getServiceNameAttributeName()));
            sp.setDescription(request.getFacilityDescription(appConfig.getServiceDescAttributeName()));
            providedServiceManager.update(sp);
        } catch (ConnectorException | JsonProcessingException e) {
            log.warn("Caught ConnectorException", e);
            try {
                perunConnector.updateFacilityInPerun(actualFacility.toJson());
                ArrayNode oldAttrsArray = JsonNodeFactory.instance.arrayNode();
                oldAttributes.values().forEach(a -> oldAttrsArray.add(a.toJson()));
                perunConnector.setFacilityAttributes(actualFacility.getId(), oldAttrsArray);
                sp.setDescription(oldDesc);
                sp.setName(oldName);
                providedServiceManager.update(sp);
            } catch (ConnectorException ex) {
                log.warn("Caught ConnectorException", ex);
            } catch (JsonProcessingException ex) {
                log.warn("caught JsonProcessingException", ex);
            }
            return false;
        }
        return true;
    }

    private boolean deleteFacilityFromPerun(Request request) throws ConnectorException, InternalErrorException {
        log.trace("deleteFacilityFromPerun({})", request);

        if (Utils.checkParamsInvalid(request)) {
            log.error("Wrong parameters passed: (request: {})", request);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        Long facilityId = extractFacilityIdFromRequest(request);

        PerunAttribute adminsGroupAttr = perunConnector.getFacilityAttribute(request.getFacilityId(), appConfig.getAdminsGroupAttribute());
        if (adminsGroupAttr == null || adminsGroupAttr.valueAsLong() == null) {
            log.warn("No admins group ID found for facility: {}", facilityId);
        } else {
            Long groupId = adminsGroupAttr.valueAsLong();
            boolean removedGroupFromAdmins = perunConnector.removeGroupFromAdmins(request.getFacilityId(), groupId);
            if (removedGroupFromAdmins) {
                perunConnector.deleteGroup(groupId);
            }
        }

        boolean facilityRemoved = perunConnector.deleteFacilityFromPerun(facilityId);
        providedServiceManager.deleteByFacilityId(facilityId);

        if (!facilityRemoved) {
            log.error("Some operations failed - facilityRemoved: {}", false);
        } else {
            log.info("Facility has been deleted");
        }

        log.trace("deleteFacilityFromPerun returns: {}", facilityRemoved);
        return facilityRemoved;
    }

    private boolean moveToProduction(Request request) throws ConnectorException {
        log.trace("requestMoveToProduction({})", request);

        log.info("Updating facility attributes");
        PerunAttribute testSp = generateTestSpAttribute(false);
        PerunAttribute showOnServiceList = generateShowOnServiceListAttribute(true);

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

        boolean attributesSet = perunConnector.setFacilityAttributes(request.getFacilityId(), attributes);

        log.trace("requestMoveToProduction returns: {}", attributesSet);
        return attributesSet;
    }

    private Long extractFacilityIdFromRequest(Request request) throws InternalErrorException {
        log.trace("extractFacilityIdFromRequest({})", request);

        if (Utils.checkParamsInvalid(request)) {
            log.error("Wrong parameters passed: (request: {})",request);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        Long facilityId = request.getFacilityId();
        if (facilityId == null) {
            log.error("Request: {} does not have facilityId", request);
            throw new InternalErrorException(Utils.GENERIC_ERROR_MSG);
        }

        log.trace("extractFacilityIdFromRequest() returns: {}", facilityId);
        return facilityId;
    }

    private PerunAttribute generateAuthProtocolAttribute(boolean isOidc) {
        log.trace("generateAuthProtocolAttribute({})", isOidc);

        PerunAttribute attribute = new PerunAttribute();
        if (isOidc) {
            attribute.setDefinition(appConfig.getAttrDefinition(appConfig.getIsOidcAttributeName()));
        } else {
            attribute.setDefinition(appConfig.getAttrDefinition(appConfig.getIsSamlAttributeName()));
        }
        attribute.setValue(true);

        log.trace("generateAuthProtocolAttribute() returns: {}", attribute);
        return attribute;
    }

    private PerunAttribute generateMasterProxyIdentifierAttribute() {
        log.trace("generateMasterProxyIdentifierAttribute()");

        PerunAttribute attribute = new PerunAttribute();
        attribute.setDefinition(appConfig.getAttrDefinition(appConfig.getMasterProxyIdentifierAttribute()));
        attribute.setValue(appConfig.getMasterProxyIdentifierAttributeValue());

        log.trace("generateMasterProxyIdentifierAttribute() returns: {}", attribute);
        return attribute;
    }

    private PerunAttribute generateProxyIdentifiersAttribute() {
        log.trace("generateProxyIdentifierAttribute()");

        PerunAttribute attribute = new PerunAttribute();
        attribute.setDefinition(appConfig.getAttrDefinition(appConfig.getProxyIdentifierAttribute()));
        attribute.setValue(Collections.singletonList(appConfig.getProxyIdentifierAttributeValue()));

        log.trace("generateProxyIdentifierAttribute() returns: {}", attribute);
        return attribute;
    }

    private PerunAttribute generateShowOnServiceListAttribute(boolean value) {
        log.trace("generateShowOnServiceListAttribute({})", value);

        PerunAttribute attribute = new PerunAttribute();
        attribute.setDefinition(appConfig.getAttrDefinition(appConfig.getShowOnServicesListAttributeName()));
        attribute.setValue(value);

        log.trace("generateShowOnServiceListAttribute() returns: {}", attribute);
        return attribute;
    }

    private PerunAttribute generateTestSpAttribute(boolean value) {
        log.trace("generateTestSpAttribute({})", value);

        PerunAttribute attribute = new PerunAttribute();
        attribute.setDefinition(appConfig.getAttrDefinition(appConfig.getIsTestSpAttribute()));
        attribute.setValue(value);

        log.trace("generateTestSpAttribute() returns: {}", attribute);
        return attribute;
    }

    private PerunAttribute generateClientIdAttribute() {
        log.trace("generateClientIdAttribute()");

        PerunAttribute attribute = new PerunAttribute();
        attribute.setDefinition(appConfig.getAttrDefinition(appConfig.getClientIdAttribute()));

        String clientId = ServiceUtils.generateClientId();
        attribute.setValue(clientId);

        log.trace("generateClientIdAttribute() returns: {}", attribute);
        return attribute;
    }

    private PerunAttribute generateAdminsGroupAttr(Long id) {
        log.trace("generateAdminsGroupAttr({})", id);

        PerunAttribute attribute = new PerunAttribute();
        attribute.setDefinition(appConfig.getAttrDefinition(appConfig.getAdminsGroupAttribute()));
        attribute.setValue(id);

        log.trace("generateAdminsGroupAttr({}) returns: {}", id, attribute);
        return attribute;
    }

    private List<PerunAttribute> filterNotNullAttributes(Facility facility) {
        log.trace("filterNotNullAttributes({})", facility);

        if (Utils.checkParamsInvalid(facility)) {
            log.error("Wrong parameters passed: (facility: {})", facility);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        List<PerunAttribute> filteredAttributes = new ArrayList<>();
        facility.getAttributes()
                .values()
                .forEach(
                        attrsInCategory -> attrsInCategory.values()
                                .stream()
                                .filter(attr -> attr.getValue() != null)
                                .forEach(filteredAttributes::add)
                );

        log.trace("filterNotNullAttributes() returns: {}", filteredAttributes);
        return filteredAttributes;
    }

    private Map<String, String> generateCodesForAuthorities(Request request, List<String> authorities, User user)
            throws InternalErrorException
    {
        log.trace("generateCodesForAuthorities(request: {}, authorities: {})", request, authorities);

        List<String> emails = new ArrayList<>();
        if (Utils.checkParamsInvalid(request, authorities)) {
            log.error("Wrong parameters passed: (request: {}, authorities: {})", request, authorities);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        if (authorities == null || authorities.isEmpty()) {
            emails = defaultAuthorities;
        } else {
            Map<String, List<String>> authsMap = appConfig.getProdTransferAuthoritiesMailsMap();
            for (String authoritiesInput: authorities) {
                if (authsMap.containsKey(authoritiesInput)) {
                    emails.addAll(authsMap.get(authoritiesInput));
                }
            }
        }

        List<LinkCode> codes = new ArrayList<>();
        Map<String, String> authsCodesMap = new HashMap<>();

        for (String authority : emails) {
            LinkCode code = createRequestCode(authority, user, request.getReqId(), request.getFacilityId());
            codes.add(code);
            authsCodesMap.put(authority, code.getHash());
        }

        linkCodeManager.createMultiple(codes);

        log.trace("generateCodesForAuthorities() returns: {}", authsCodesMap);
        return authsCodesMap;
    }

    private LinkCode createRequestCode(String authority, User user, Long requestId, Long facilityId) {
        log.trace("createRequestCode(authority: {}, user: {}, requestId: {}, facilityId: {})",
                authority, user, requestId, facilityId);
        LinkCode code = new LinkCode();

        code.setRecipientEmail(authority);
        code.setSenderName(user.getName());
        code.setSenderEmail(user.getEmail());
        code.setExpiresAt(appConfig);
        code.setFacilityId(facilityId);
        code.setRequestId(requestId);
        code.setHash(ServiceUtils.getHash(code.toString()));

        log.trace("createRequestCode(authority: {}, user: {}, requestId: {}, facilityId: {}) returns: {}",
                authority, user, requestId, facilityId, code);
        return code;
    }

    private Map<String, String> generateLinksForAuthorities(Map<String, String> authorityCodeMap)
            throws UnsupportedEncodingException {
        log.trace("generateLinksForAuthorities(authorityCodeMap: {})", authorityCodeMap);

        if (Utils.checkParamsInvalid(authorityCodeMap)) {
            log.error("Wrong parameters passed: (authorityCodeMap: {})", authorityCodeMap);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        Map<String, String> linksMap = new HashMap<>();

        for (Map.Entry<String, String> entry : authorityCodeMap.entrySet()) {
            String code = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString());
            String link = appConfig.getSignaturesEndpointUrl()
                    .concat("?code=").concat(code);
            linksMap.put(entry.getKey(), link);
            log.debug("Generated code: {}", code); //TODO: remove
        }

        log.trace("generateLinksForAuthorities() returns: {}", linksMap);
        return linksMap;
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
            keptAttrs.add(appConfig.getClientIdAttribute());
            keptAttrs.add(appConfig.getClientSecretAttribute());
        } else {
            keptAttrs.addAll(config.getSamlInputs()
                    .stream()
                    .map(AttrInput::getName)
                    .collect(Collectors.toList())
            );
        }

        return keptAttrs;
    }

}
