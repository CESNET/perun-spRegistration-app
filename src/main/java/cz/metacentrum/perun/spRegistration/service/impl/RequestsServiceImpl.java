package cz.metacentrum.perun.spRegistration.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.metacentrum.perun.spRegistration.Utils;
import cz.metacentrum.perun.spRegistration.common.ExecuteAndSwallowException;
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
import cz.metacentrum.perun.spRegistration.common.exceptions.ProcessingException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.AuditLog;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.Group;
import cz.metacentrum.perun.spRegistration.common.models.InputsContainer;
import cz.metacentrum.perun.spRegistration.common.models.LinkCode;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.ProvidedService;
import cz.metacentrum.perun.spRegistration.common.models.RequestDTO;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.enums.ServiceEnvironment;
import cz.metacentrum.perun.spRegistration.persistence.enums.ServiceProtocol;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.persistence.managers.AuditLogsManager;
import cz.metacentrum.perun.spRegistration.persistence.managers.LinkCodeManager;
import cz.metacentrum.perun.spRegistration.persistence.managers.ProvidedServiceManager;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestManager;
import cz.metacentrum.perun.spRegistration.service.FacilitiesService;
import cz.metacentrum.perun.spRegistration.service.MailsService;
import cz.metacentrum.perun.spRegistration.service.RequestsService;
import cz.metacentrum.perun.spRegistration.service.ServiceUtils;
import cz.metacentrum.perun.spRegistration.service.UtilsService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cz.metacentrum.perun.spRegistration.common.enums.RequestStatus.APPROVED;
import static cz.metacentrum.perun.spRegistration.common.enums.RequestStatus.REJECTED;
import static cz.metacentrum.perun.spRegistration.common.enums.RequestStatus.WAITING_FOR_APPROVAL;
import static cz.metacentrum.perun.spRegistration.common.enums.RequestStatus.WAITING_FOR_CHANGES;
import static cz.metacentrum.perun.spRegistration.service.impl.MailsServiceImpl.LANG_EN;
import static cz.metacentrum.perun.spRegistration.service.impl.MailsServiceImpl.REQUEST_CREATED;
import static cz.metacentrum.perun.spRegistration.service.impl.MailsServiceImpl.REQUEST_MODIFIED;

@Service("requestsService")
@Slf4j
public class RequestsServiceImpl implements RequestsService {

    @NonNull private final PerunAdapter perunAdapter;
    @NonNull private final MailsService mailsService;
    @NonNull private final UtilsService utilsService;
    @NonNull private final RequestManager requestManager;
    @NonNull private final LinkCodeManager linkCodeManager;
    @NonNull private final AuditLogsManager auditLogsManager;
    @NonNull private final FacilitiesService facilitiesService;
    @NonNull private final AppBeansContainer applicationBeans;
    @NonNull private final ApplicationProperties applicationProperties;
    @NonNull private final AttributesProperties attributesProperties;
    @NonNull private final ApprovalsProperties approvalsProperties;
    @NonNull private final ProvidedServiceManager providedServiceManager;
    @NonNull private final InputsContainer inputsContainer;

    @Autowired
    public RequestsServiceImpl(@NonNull PerunAdapter perunAdapter,
                               @NonNull MailsService mailsService,
                               @NonNull UtilsService utilsService,
                               @NonNull RequestManager requestManager,
                               @NonNull LinkCodeManager linkCodeManager,
                               @NonNull AuditLogsManager auditLogsManager,
                               @NonNull FacilitiesService facilitiesService,
                               @NonNull AppBeansContainer applicationBeans,
                               @NonNull ApplicationProperties applicationProperties,
                               @NonNull AttributesProperties attributesProperties,
                               @NonNull ApprovalsProperties approvalsProperties,
                               @NonNull ProvidedServiceManager providedServiceManager,
                               @NonNull InputsContainer inputsContainer)
    {
        this.perunAdapter = perunAdapter;
        this.mailsService = mailsService;
        this.utilsService = utilsService;
        this.requestManager = requestManager;
        this.linkCodeManager = linkCodeManager;
        this.auditLogsManager = auditLogsManager;
        this.facilitiesService = facilitiesService;
        this.applicationBeans = applicationBeans;
        this.applicationProperties = applicationProperties;
        this.attributesProperties = attributesProperties;
        this.approvalsProperties = approvalsProperties;
        this.providedServiceManager = providedServiceManager;
        this.inputsContainer = inputsContainer;
    }

    @Override
    public Long createRegistrationRequest(@NonNull User user, @NonNull List<PerunAttribute> attributes)
            throws InternalErrorException
    {
        if (attributes.isEmpty()) {
            log.error("No attributes provided");
            throw new IllegalArgumentException("Attribute list is empty");
        }

        RequestDTO req;
        try {
            req = createRequest(null, user, RequestAction.REGISTER_NEW_SP, attributes);
        } catch (ActiveRequestExistsException e) {
            //this should not happen as the registration is for new service and thus facility id will be always null
            log.error("Caught {} when creating registration of a new service", e.getClass().getSimpleName(), e);
            throw new InternalErrorException("Could not create request");
        }

        mailsService.notifyUser(req, REQUEST_CREATED);
        mailsService.notifyAppAdmins(req, REQUEST_CREATED);

        insertRegisterServiceReqCreatedAuditLog(req.getReqId(), user.getId(), user.getName());
        return req.getReqId();
    }

    @Override
    public Long createFacilityChangesRequest(@NonNull Long facilityId, @NonNull User user,
                                             @NonNull List<PerunAttribute> attributes)
            throws InternalErrorException, ActiveRequestExistsException, PerunUnknownException, PerunConnectionException
    {
        if (attributes.isEmpty()) {
            throw new IllegalArgumentException("Attributes cannot be empty");
        }

        Facility facility = perunAdapter.getFacilityById(facilityId);
        if (facility == null) {
            throw new InternalErrorException("Could not fetch facility for facilityId: " + facilityId);
        }

        boolean attrsChanged = false;
        List<String> attrNames = attributes.stream().map(PerunAttribute::getFullName).collect(Collectors.toList());
        Map<String, PerunAttribute> actualAttrs = perunAdapter.getFacilityAttributes(facilityId, attrNames);
        for (PerunAttribute a: attributes) {
            if (actualAttrs.containsKey(a.getFullName())) {
                PerunAttribute actualA = actualAttrs.get(a.getFullName());
                if (!Objects.equals(a.getValue(), actualA.getValue())) {
                    attrsChanged = true;
                }
                a.setOldValue(actualA.getDefinition().getType(), actualA.getValue());
            }
        }

        attributes = attributes.stream().filter(a -> !isOidcCredentialAttr(a)).collect(Collectors.toList());

        if (!attrsChanged) {
            return null;
        }

        RequestDTO req = createRequest(facilityId, user, RequestAction.UPDATE_FACILITY, attributes);

        mailsService.notifyUser(req, REQUEST_CREATED);
        mailsService.notifyAppAdmins(req, REQUEST_CREATED);

        insertUpdateServiceReqCreatedAuditLog(req.getReqId(), user.getId(), user.getName());
        return req.getReqId();
    }

    @Override
    public Long createRemovalRequest(@NonNull User user, @NonNull Long facilityId)
            throws InternalErrorException, ActiveRequestExistsException, PerunUnknownException, PerunConnectionException
    {
        List<PerunAttribute> facilityAttributes = ServiceUtils.getFacilityAttributes(applicationBeans, facilityId,
                attributesProperties, inputsContainer, perunAdapter);

        facilityAttributes = facilityAttributes.stream()
                .filter(a -> !isOidcCredentialAttr(a, false))
                .collect(Collectors.toList());
        RequestDTO req = createRequest(facilityId, user, RequestAction.DELETE_FACILITY, facilityAttributes);

        mailsService.notifyUser(req, REQUEST_CREATED);
        mailsService.notifyAppAdmins(req, REQUEST_CREATED);

        insertRemoveServiceReqCreatedAuditLog(req.getReqId(), user.getId(), user.getName());
        return req.getReqId();
    }

    @Override
    public Long createMoveToProductionRequest(@NonNull Long facilityId, @NonNull User user,
                                              @NonNull List<String> authorities)
            throws InternalErrorException, ActiveRequestExistsException, BadPaddingException, InvalidKeyException,
            IllegalBlockSizeException, UnsupportedEncodingException, PerunUnknownException, PerunConnectionException,
            UnauthorizedActionException
    {
        Facility fac = facilitiesService.getFacility(facilityId, user.getId(), false);
        if (fac == null) {
            throw new InternalErrorException("Could not retrieve facility for id: " + facilityId);
        }

        List<PerunAttribute> attrs = new ArrayList<>();
        fac.getAttributes().values().stream().map(Map::values).forEach(attrs::addAll);
        attrs = attrs.stream().filter(a -> !isOidcCredentialAttr(a, false)).collect(Collectors.toList());

        RequestDTO req = createRequest(facilityId, user, RequestAction.MOVE_TO_PRODUCTION, attrs);

        Map<String, String> authoritiesLinksMap = generateLinksForAuthorities(req, authorities, user);

        mailsService.notifyUser(req, REQUEST_CREATED);
        mailsService.notifyAppAdmins(req, REQUEST_CREATED);
        mailsService.notifyAuthorities(req, authoritiesLinksMap);

        insertTransferServiceReqCreatedAuditLog(req.getReqId(), user.getId(), user.getName());
        return req.getReqId();
    }

    @Override
    public boolean updateRequest(@NonNull Long requestId, @NonNull User user,
                                 @NonNull List<PerunAttribute> attributes)
            throws UnauthorizedActionException, InternalErrorException, PerunUnknownException, PerunConnectionException
    {
        RequestDTO request = requestManager.getRequestById(requestId);
        if (request == null) {
            throw new InternalErrorException("Could not retrieve request for id: " + requestId);
        } else if (!utilsService.isAdminForRequest(request, user.getId())) {
            throw new UnauthorizedActionException("User is not registered as admin in request, cannot update it");
        }

        attributes = attributes.stream().filter(a -> !isOidcCredentialAttr(a)).collect(Collectors.toList());

        request.updateAttributes(attributes, true, applicationBeans);
        request.setStatus(WAITING_FOR_APPROVAL);
        request.setModifiedBy(user.getId());
        request.setModifiedAt(new Timestamp(System.currentTimeMillis()));

        if (!requestManager.updateRequest(request)) {
            return false;
        }

        mailsService.notifyUser(request, REQUEST_MODIFIED);
        mailsService.notifyAppAdmins(request, REQUEST_MODIFIED);

        insertUpdatedRequestAuditLog(requestId, user.getId(), user.getName());
        return true;
    }

    @Override
    public RequestDTO getRequestForSignatureByCode(@NonNull String code)
            throws ExpiredCodeException, InternalErrorException
    {
        LinkCode linkCode = linkCodeManager.get(code);
        if (linkCode == null) {
            throw new ExpiredCodeException("Code has expired");
        } else if (linkCode.getRequestId() == null) {
            throw new InternalErrorException("Code has no request id");
        }

        Long requestId = linkCode.getRequestId();
        RequestDTO request = requestManager.getRequestById(requestId);
        if (request == null) {
            throw new InternalErrorException("Cannot find request from code");
        }
        return request;
    }

    @Override
    public RequestDTO getRequest(@NonNull Long requestId, @NonNull  User user)
            throws UnauthorizedActionException, InternalErrorException, PerunUnknownException, PerunConnectionException
    {
        RequestDTO request = requestManager.getRequestById(requestId);
        if (request == null) {
            throw new InternalErrorException("Could not retrieve request for id: " + requestId);
        } else if (!utilsService.isAdminForRequest(request, user.getId())) {
            throw new UnauthorizedActionException("User cannot view request, user is not a requester");
        }

        if (request.getReqUserId() != null) {
            try {
                User requester = perunAdapter.getUserById(request.getReqUserId());
                request.setRequester(requester);
                User modifier = perunAdapter.getUserById(request.getModifiedBy());
                request.setModifier(modifier);
            } catch (PerunConnectionException | PerunUnknownException e) {
                log.warn("Could not fetch requester or modifier of request", e);
            }
        }

        return request;
    }

    @Override
    public List<RequestDTO> getAllUserRequests(@NonNull User user)
            throws PerunUnknownException, PerunConnectionException
    {
        Set<RequestDTO> requests = new HashSet<>();
        List<RequestDTO> userRequests = requestManager.getAllRequestsByUserId(user.getId());
        if (userRequests != null && !userRequests.isEmpty()) {
            requests.addAll(userRequests);
        }

        Set<Long> facilityIdsWhereUserIsAdmin = perunAdapter.getFacilityIdsWhereUserIsAdmin(user.getId());
        if (facilityIdsWhereUserIsAdmin != null && !facilityIdsWhereUserIsAdmin.isEmpty()) {
            List<RequestDTO> facilitiesRequests = requestManager.getAllRequestsByFacilityIds(facilityIdsWhereUserIsAdmin);
            if (facilitiesRequests != null && !facilitiesRequests.isEmpty()) {
                requests.addAll(facilitiesRequests);
            }
        }
        mapProvidedServicesToRequests(requests);

        return new ArrayList<>(requests);
    }

    @Override
    public boolean cancelRequest(Long requestId, User user)
            throws UnauthorizedActionException, InternalErrorException, PerunUnknownException, PerunConnectionException
    {
        if (Utils.checkParamsInvalid(requestId, user.getId())) {
            log.error("Wrong parameters passed: (user.getId(): {}, action: {})", user.getId(), user.getId());
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }
        RequestDTO request = requestManager.getRequestById(requestId);
        if (request == null) {
            log.error("Could not fetch request with ID: {} from database", requestId);
            throw new InternalErrorException("Could not fetch request with ID: " + requestId + " from database");
        } else if (!applicationProperties.isAppAdmin(user.getId())
                && !utilsService.isAdminForRequest(request, user.getId())) {
            throw new UnauthorizedActionException("Cannot cancel request");
        }

        if (RequestAction.MOVE_TO_PRODUCTION.equals(request.getAction())) {
            int removedCodes = linkCodeManager.deleteForRequest(request.getReqId());
            log.info("Removed {} codes", removedCodes);
        }

        request.setModifiedBy(user.getId());
        request.setStatus(RequestStatus.CANCELED);
        request.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        boolean requestUpdated = requestManager.updateRequest(request);
        mailsService.notifyUser(request, MailsServiceImpl.REQUEST_CANCELED);

        if (!requestUpdated) {
            log.error("some operations failed: requestUpdated: false for request: {}", request);
        } else {
            log.info("Request updated, notification sent");
        }

        insertCancelRequestAuditLog(requestId, user.getId(), user.getName());
        return requestUpdated;
    }

    @Override
    public List<RequestDTO> getAllRequests(@NonNull User user) throws UnauthorizedActionException {
        if (!applicationProperties.isAppAdmin(user.getId())) {
            throw new UnauthorizedActionException("User not admin");
        }
        List<RequestDTO> requests = requestManager.getAllRequests();
        mapProvidedServicesToRequests(requests);
        return requests;
    }

    private void mapProvidedServicesToRequests(Collection<RequestDTO> requests) {
        Set<Long> facIds = requests.stream()
                .map(RequestDTO::getFacilityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<ProvidedService> services = providedServiceManager.getAllForFacilities(facIds);
        Map<Long, ProvidedService> providedServiceMap = new HashMap<>();
        services.forEach(s -> providedServiceMap.put(s.getFacilityId(), s));
        requests.forEach(r -> r.setProvidedService(providedServiceMap.get(r.getFacilityId())));
    }

    @Override
    public boolean approveRequest(@NonNull Long requestId, @NonNull User user)
            throws CannotChangeStatusException, InternalErrorException, PerunUnknownException, PerunConnectionException
    {
        RequestDTO request = requestManager.getRequestById(requestId);
        if (request == null) {
            throw new InternalErrorException("Could not fetch request with ID: " + requestId + " from database");
        } else if (!hasCorrectStatus(request.getStatus(),
                new RequestStatus[] {WAITING_FOR_CHANGES, WAITING_FOR_APPROVAL}))
        {
            throw new CannotChangeStatusException("Cannot approve request, request is not in valid status");
        }

        RequestStatus oldStatus = request.getStatus();

        request.setStatus(APPROVED);
        request.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        request.setModifiedBy(user.getId());
        request.updateAttributes(new ArrayList<>(), false, applicationBeans);

        if (!processApprovedRequest(request)) {
            return false;
        }

        if (!requestManager.updateRequest(request)) {
            return false;
        }

        AuditLog audit = new AuditLog();
        audit.setActorId(user.getId());
        insertApproveRequestAuditLog(requestId, user.getId(), user.getName());
        mailsService.notifyUser(request, MailsServiceImpl.REQUEST_STATUS_UPDATED);
        return true;
    }

    @Override
    public boolean rejectRequest(@NonNull Long requestId, @NonNull User user)
            throws CannotChangeStatusException, InternalErrorException
    {
        RequestDTO request = requestManager.getRequestById(requestId);
        if (request == null) {
            throw new InternalErrorException("Could not fetch request with ID: " + requestId + " from database");
        } else if (!hasCorrectStatus(request.getStatus(), new RequestStatus[] {WAITING_FOR_APPROVAL, WAITING_FOR_CHANGES})) {
            throw new CannotChangeStatusException("Cannot reject request, request is not in valid status");
        }

        RequestStatus oldStatus = request.getStatus();

        request.setStatus(REJECTED);
        request.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        request.setModifiedBy(user.getId());

        if (!requestManager.updateRequest(request)) {
            return false;
        }
        insertRejectRequestAuditLog(requestId, user.getId(), user.getName());
        mailsService.notifyUser(request, MailsServiceImpl.REQUEST_STATUS_UPDATED);
        return true;
    }

    @Override
    public boolean askForChanges(@NonNull Long requestId, @NonNull User user,
                                 @NonNull List<PerunAttribute> attributes)
            throws CannotChangeStatusException, InternalErrorException
    {
        RequestDTO request = requestManager.getRequestById(requestId);
        if (request == null) {
            throw new InternalErrorException("Could not fetch request with ID: " + requestId + " from database");
        } else if (!hasCorrectStatus(request.getStatus(), new RequestStatus[] {WAITING_FOR_APPROVAL, WAITING_FOR_CHANGES})) {
            throw new CannotChangeStatusException("Cannot ask for changes, request not marked as WFA nor WFC");
        }

        RequestStatus oldStatus = request.getStatus();

        request.updateAttributes(attributes, false, applicationBeans);
        request.setStatus(WAITING_FOR_CHANGES);
        request.setModifiedBy(user.getId());
        request.setModifiedAt(new Timestamp(System.currentTimeMillis()));

        if (!requestManager.updateRequest(request)) {
            return false;
        }

        insertChangesRequestedRequestAuditLog(requestId, user.getId(), user.getName());
        mailsService.notifyUser(request, MailsServiceImpl.REQUEST_STATUS_UPDATED);
        return true;
    }

    // private methods

    private RequestDTO createRequest(Long facilityId, User user,
                                     RequestAction action, List<PerunAttribute> attributes)
            throws InternalErrorException, ActiveRequestExistsException
    {
        RequestDTO request = new RequestDTO();
        request.setFacilityId(facilityId);
        request.setStatus(WAITING_FOR_APPROVAL);
        request.setAction(action);
        request.updateAttributes(attributes, true, applicationBeans);
        request.setReqUserId(user.getId());
        request.setModifiedBy(user.getId());
        request.setModifiedAt(new Timestamp(System.currentTimeMillis()));

        Long requestId = requestManager.createRequest(request);
        if (requestId == null) {
            throw new InternalErrorException("Could not create request in DB");
        }
        request.setReqId(requestId);
        return request;
    }

    private boolean insertAuditLog(Long requestId, Long actorId, String actorName, String message) {
        AuditLog audit = new AuditLog();
        audit.setRequestId(requestId);
        audit.setActorId(actorId);
        audit.setActorName(actorName);
        audit.setMessage(message);
        try {
            audit = auditLogsManager.insert(audit);
            return audit != null;
        } catch (InternalErrorException e) {
            log.warn("Failed to insert audit log '{}'", audit);
            //TODO: implement rescheduling of such tasks
            return false;
        }
    }

    private boolean processApprovedRequest(RequestDTO request)
            throws InternalErrorException, PerunUnknownException, PerunConnectionException
    {
        switch(request.getAction()) {
            case REGISTER_NEW_SP:
                return registerNewService(request);
            case UPDATE_FACILITY:
                return updateFacilityInPerun(request);
            case DELETE_FACILITY:
                return deleteFacilityFromPerun(request);
            case MOVE_TO_PRODUCTION:
                return moveToProduction(request.getFacilityId());
        }
        return false;
    }

    private boolean registerNewService(RequestDTO request) throws InternalErrorException {
        String name = getNameFromRequest(request);
        PerunAttribute clientId = generateClientIdAttribute();
        String desc = getDescFromRequest(request, clientId);

        Facility facility;
        try {
            facility = perunAdapter.createFacilityInPerun(name, desc);
            if (facility == null || facility.getId() == null || !StringUtils.hasText(facility.getPerunName())) {
                log.error("Creating facility in Perun has failed");
                return false;
            }
            request.setFacilityId(facility.getId());
        } catch (PerunUnknownException | PerunConnectionException e) {
            log.error("Creating facility in Perun has failed");
            return false;
        }

        ProvidedService sp = null;
        Long adminsGroupId = null;
        try {
            sp = createSp(facility.getId(), request, clientId);
            if (sp == null) {
                throw new InternalErrorException("Could not create SP");
            }
            adminsGroupId = createAdminsGroup(facility.getPerunName());
            if (!setUserAsServiceManager(facility.getId(), adminsGroupId, request.getReqUserId())) {
                throw new InternalErrorException("Could not set managers group");
            }
            if (!setFacilityAttributes(request, facility.getId(), adminsGroupId, clientId)) {
                throw new InternalErrorException("Setting new attributes has failed");
            }
        } catch (Exception e) {
            log.error("Caught an exception when processing approved request to create service", e);
            if (sp != null) {
                final Long spId = sp.getId();
                ((ExecuteAndSwallowException) () -> providedServiceManager.delete(spId)).execute(log);
            }
            if (adminsGroupId != null) {
                final Long admGrId = adminsGroupId;
                ((ExecuteAndSwallowException) () -> perunAdapter.deleteGroup(admGrId)).execute(log);
            }
            if (facility.getId() != null) {
                final Long facId = facility.getId();
                ((ExecuteAndSwallowException) () -> perunAdapter.deleteFacilityFromPerun(facId)).execute(log);
            }
            return false;
        }
        return true;
    }

    private boolean updateFacilityInPerun(RequestDTO request)
            throws InternalErrorException, PerunUnknownException, PerunConnectionException
    {
        Long facilityId = extractFacilityIdFromRequest(request);
        Facility actualFacility = perunAdapter.getFacilityById(facilityId);
        if (actualFacility == null) {
            log.error("Facility with ID {} does not exist in Perun", facilityId);
            return false;
        }

        Map<String, PerunAttribute> oldAttributes = perunAdapter.getFacilityAttributes(facilityId,
                request.getAttributeNames());
        if (oldAttributes == null || oldAttributes.isEmpty()) {
            log.error("Could not fetch actual attributes for facility with id {}", facilityId);
            return false;
        }

        final ProvidedService sp = providedServiceManager.getByFacilityId(facilityId);
        if (sp == null) {
            log.error("Could not fetch SP from local DB (facility id = {})", facilityId);
            return false;
        }

        Map<String, String> oldName = sp.getName();
        Map<String, String> oldDesc = sp.getDescription();
        boolean spUpdateRollback = false;
        try {
            if (!perunAdapter.setFacilityAttributes(request.getFacilityId(),
                    request.getAttributesAsJsonArrayForPerun())) {
                throw new InternalErrorException("Failed to update attributes to new ones");
            }
            spUpdateRollback = true;
            sp.setName(request.getFacilityName(attributesProperties.getNames().getServiceName()));
            sp.setDescription(request.getFacilityDescription(attributesProperties.getNames().getServiceDesc()));
            if (!providedServiceManager.update(sp)) {
                throw new InternalErrorException("Failed to update SP in local DB");
            }
        } catch (Exception e)
        {
            rollBackAttrChanges(oldAttributes, actualFacility.getId());
            if (spUpdateRollback) {
                rollBackSpChanges(sp, oldName, oldDesc, sp.getEnvironment());
            }
            return false;
        }
        return true;
    }

    private boolean deleteFacilityFromPerun(RequestDTO request) throws InternalErrorException {
        Long facilityId = extractFacilityIdFromRequest(request);
        boolean spDeleted = false;
        ProvidedService sp = null;
        try {
            try {
                sp = providedServiceManager.getByFacilityId(facilityId);
                if (sp != null) {
                    spDeleted = providedServiceManager.delete(sp.getId());
                }
            } catch (InternalErrorException e) {
                log.error("Failed to delete provided service");
                throw new ProcessingException("Failed to delete provided service");
            }
            if (!spDeleted) {
                throw new ProcessingException("Failed to delete SP object in local storage");
            }
            if (!deleteFacility(sp, facilityId)) {
                throw new ProcessingException("Failed to delete facility from perun");
            }
            deleteAdminsGroup(facilityId);
        } catch (Exception e) {
            if (spDeleted) {
                final ProvidedService spToRecreate = sp;
                ((ExecuteAndSwallowException) () -> providedServiceManager.recreate(spToRecreate)).execute(log);
            }
            log.error("Caught Processing exception: {}", e.getMessage(), e);
            return false;
        }

        return true;
    }

    private boolean moveToProduction(Long facilityId) {
        PerunAttribute testSp = generateTestSpAttribute(false);
        PerunAttribute showOnServiceList = generateShowOnServiceListAttribute(true);

        ArrayNode attributes = JsonNodeFactory.instance.arrayNode();
        attributes.add(testSp.toJson());
        attributes.add(showOnServiceList.toJson());

        ProvidedService sp = providedServiceManager.getByFacilityId(facilityId);
        sp.setEnvironment(ServiceEnvironment.PRODUCTION);
        try {
            providedServiceManager.update(sp);
        } catch (InternalErrorException | JsonProcessingException ex) {
            log.warn("Could not update SP to Production environment: {}", sp, ex);
            return false;
        }

        try {
            if (!perunAdapter.setFacilityAttributes(facilityId, attributes)) {
                throw new ProcessingException("Failed to update attributes");
            }
        } catch (Exception e) {
            rollBackSpChanges(sp, sp.getName(), sp.getDescription(), ServiceEnvironment.TESTING);
            return false;
        }
        return true;
    }

    private boolean setFacilityAttributes(RequestDTO request, Long facilityId, Long adminsGroupId, PerunAttribute clientId) {
        try {
            ArrayNode attributes = request.getAttributesAsJsonArrayForPerun();
            boolean isOidc = ServiceUtils.isOidcRequest(request, attributesProperties.getNames().getEntityId());
            if (isOidc) {
                if (!setClientIdAttribute(facilityId, clientId)) {
                    return false;
                }
                PerunAttribute clientSecret = utilsService.generateClientSecretAttribute();
                perunAdapter.setFacilityAttribute(facilityId, clientSecret.toJson());
            }
            fillAttributes(attributes, isOidc, adminsGroupId);
            return perunAdapter.setFacilityAttributes(request.getFacilityId(), attributes);
        } catch (Exception e)
        {
            log.error("Failure when setting attributes: {}", e.getMessage(), e);
            return false;
        }
    }

    private ProvidedService createSp(Long facilityId, RequestDTO request, PerunAttribute clientId) {
        ProvidedService sp = new ProvidedService();

        sp.setFacilityId(facilityId);
        sp.setName(request.getFacilityName(attributesProperties.getNames().getServiceName()));
        sp.setDescription(request.getFacilityDescription(attributesProperties.getNames().getServiceDesc()));
        sp.setEnvironment(ServiceEnvironment.TESTING);
        sp.setProtocol(ServiceUtils.isOidcRequest(request, attributesProperties.getNames().getEntityId()) ?
                ServiceProtocol.OIDC : ServiceProtocol.SAML);
        sp.setIdentifier(sp.getProtocol().equals(ServiceProtocol.SAML) ?
                request.getAttributes().get(AttributeCategory.PROTOCOL)
                        .get(attributesProperties.getNames().getEntityId()).valueAsString() : clientId.valueAsString());

        try {
            sp = providedServiceManager.create(sp);
        } catch (Exception e) {
            log.error("Creating SP in DB has failed");
            return null;
        }
        return sp;
    }

    private void rollBackAttrChanges(Map<String, PerunAttribute> oldAttributes, Long facilityId) {
        ArrayNode oldAttrsArray = JsonNodeFactory.instance.arrayNode();
        oldAttributes.values().forEach(a -> oldAttrsArray.add(a.toJson()));
        ((ExecuteAndSwallowException) () -> perunAdapter.setFacilityAttributes(
                facilityId, oldAttrsArray)).execute(log);
    }

    private void rollBackSpChanges(final ProvidedService sp, Map<String, String> oldName,
                                   Map<String, String> oldDesc, ServiceEnvironment env)
    {
        sp.setDescription(oldDesc);
        sp.setName(oldName);
        sp.setEnvironment(env);
        ((ExecuteAndSwallowException) () -> providedServiceManager.update(sp)).execute(log);
    }

    private void deleteAdminsGroup(Long facilityId) throws ProcessingException {
        try {
            PerunAttribute adminsGroupAttr = perunAdapter.getFacilityAttribute(facilityId,
                    attributesProperties.getNames().getManagerGroup());
            if (adminsGroupAttr == null || adminsGroupAttr.valueAsLong() == null) {
                log.warn("No admins group ID found for facility: {}", facilityId);
            } else {
                boolean groupDeleted = perunAdapter.deleteGroup(adminsGroupAttr.valueAsLong());
                if (!groupDeleted) {
                    log.warn("Admins group {} has not been deleted", adminsGroupAttr.valueAsLong());
                    //todo: reschedule this action
                }
            }
        } catch (Exception e) {
            throw new ProcessingException("Perun exception caught when deleting admins group", e);
        }
    }

    private boolean deleteFacility(final ProvidedService sp, Long facilityId) throws ProcessingException {
        try {
            perunAdapter.deleteFacilityFromPerun(facilityId);
        } catch (Exception e) {
            final ProvidedService restored = new ProvidedService();
            restored.setFacilityId(sp.getFacilityId());
            restored.setName(sp.getName());
            restored.setDescription(sp.getDescription());
            restored.setIdentifier(sp.getIdentifier());
            restored.setEnvironment(sp.getEnvironment());
            restored.setProtocol(sp.getProtocol());
            ((ExecuteAndSwallowException) () -> providedServiceManager.create(restored)).execute(log);
            throw new ProcessingException("Failed to delete facility form perun");
        }
        return true;
    }

    private boolean setClientIdAttribute(Long facilityId, PerunAttribute clientId) {
        for (int i = 0; i < 20; i++) {
            try {
                boolean set = perunAdapter.setFacilityAttribute(facilityId, clientId.toJson());
                if (set) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("Failed to set attribute clientId with value {} for facility {}",
                        clientId.valueAsString(), facilityId);
                clientId = generateClientIdAttribute();
            }
        }
        return false;
    }

    private void fillAttributes(ArrayNode attributes, boolean isOidcService, Long adminsGroupId)
            throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException
    {
        if (isOidcService) {
            PerunAttribute clientSecret = utilsService.generateClientSecretAttribute();
            attributes.add(clientSecret.toJson());
        }

        attributes.add(generateTestSpAttribute(true).toJson());
        attributes.add(generateShowOnServiceListAttribute(false).toJson());
        attributes.add(generateProxyIdentifiersAttribute().toJson());
        attributes.add(generateMasterProxyIdentifierAttribute().toJson());
        attributes.add(generateAuthProtocolAttribute(isOidcService).toJson());
        attributes.add(generateAdminsGroupAttr(adminsGroupId).toJson());
    }

    private String getDescFromRequest(RequestDTO request, PerunAttribute clientId) {
        if (ServiceUtils.isOidcRequest(request, attributesProperties.getNames().getEntityId())) {
            return clientId.valueAsString();
        } else {
            return request.getAttributes().get(AttributeCategory.PROTOCOL)
                    .get(attributesProperties.getNames().getEntityId()).valueAsString();
        }
    }

    private String getNameFromRequest(RequestDTO request) throws InternalErrorException {
        Map<String, String> nameAttrValue = request.getFacilityName(attributesProperties.getNames().getServiceName());
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

    private Long extractFacilityIdFromRequest(RequestDTO request) throws InternalErrorException {
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
            attribute.setDefinition(applicationBeans.getAttrDefinition(attributesProperties.getNames().getIsOidc()));
        } else {
            attribute.setDefinition(applicationBeans.getAttrDefinition(attributesProperties.getNames().getIsSaml()));
        }
        attribute.setValue(attribute.getDefinition().getType(), JsonNodeFactory.instance.booleanNode(true));
        return attribute;
    }

    private PerunAttribute generateMasterProxyIdentifierAttribute() {
       PerunAttribute attribute = new PerunAttribute();
        attribute.setDefinition(applicationBeans.getAttrDefinition(attributesProperties.getNames().getMasterProxyIdentifier()));
        attribute.setValue(attribute.getDefinition().getType(),  JsonNodeFactory.instance.textNode(
                attributesProperties.getValues().getMasterProxyIdentifier()));
        return attribute;
    }

    private PerunAttribute generateProxyIdentifiersAttribute() {
        PerunAttribute attribute = new PerunAttribute();
        attribute.setDefinition(applicationBeans.getAttrDefinition(attributesProperties.getNames().getProxyIdentifier()));
        ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
        arrayNode.add(attributesProperties.getValues().getProxyIdentifier());
        attribute.setValue(attribute.getDefinition().getType(),  arrayNode);
        return attribute;
    }

    private PerunAttribute generateShowOnServiceListAttribute(boolean value) {
        PerunAttribute attribute = new PerunAttribute();
        attribute.setDefinition(applicationBeans.getAttrDefinition(attributesProperties.getNames().getShowOnServiceList()));
        attribute.setValue(attribute.getDefinition().getType(),  JsonNodeFactory.instance.booleanNode(value));
        return attribute;
    }

    private PerunAttribute generateTestSpAttribute(boolean value) {
        PerunAttribute attribute = new PerunAttribute();
        attribute.setDefinition(applicationBeans.getAttrDefinition(attributesProperties.getNames().getIsTestSp()));
        attribute.setValue(attribute.getDefinition().getType(),  JsonNodeFactory.instance.booleanNode(value));
        return attribute;
    }

    private PerunAttribute generateClientIdAttribute() {
        PerunAttribute attribute = new PerunAttribute();
        attribute.setDefinition(applicationBeans.getAttrDefinition(attributesProperties.getNames().getOidcClientId()));

        String clientId = ServiceUtils.generateClientId();
        attribute.setValue(attribute.getDefinition().getType(),  JsonNodeFactory.instance.textNode(clientId));
        return attribute;
    }

    private PerunAttribute generateAdminsGroupAttr(Long id) {
        PerunAttribute attribute = new PerunAttribute();
        attribute.setDefinition(applicationBeans.getAttrDefinition(attributesProperties.getNames().getManagerGroup()));
        attribute.setValue(attribute.getDefinition().getType(),  JsonNodeFactory.instance.numberNode(id));
        return attribute;
    }

    private Map<String, String> generateCodesForAuthorities(RequestDTO request, List<String> authorities, User user)
            throws InternalErrorException
    {
        List<String> emails = new ArrayList<>();
        if (authorities == null || authorities.isEmpty()) {
            emails = approvalsProperties.getTransferAuthorities().getDefaultEntries();
            if (emails.isEmpty()) {
                return new HashMap<>();
            }
        } else {
            Map<String, List<String>> authsMap = approvalsProperties.getTransferAuthorities().getSelectionEntries();
            for (String authoritiesInput: authorities) {
                if (authsMap.containsKey(authoritiesInput)) {
                    emails.addAll(authsMap.get(authoritiesInput));
                }
            }
        }

        List<LinkCode> codes = new LinkedList<>();
        Map<String, String> authsCodesMap = new HashMap<>();

        for (String authority : emails) {
            LinkCode code = createRequestCode(authority, user, request.getReqId(), request.getFacilityId());
            codes.add(code);
            authsCodesMap.put(authority, code.getHash());
        }

        linkCodeManager.createMultiple(codes);
        return authsCodesMap;
    }

    private LinkCode createRequestCode(String authority, User user,
                                       Long requestId, Long facilityId)
    {
        LinkCode code = new LinkCode();
        code.setRecipientEmail(authority);
        code.setSenderName(user.getName());
        code.setSenderEmail(user.getEmail());
        code.setExpiresAt(approvalsProperties.getConfirmationPeriod().getDays(),
                approvalsProperties.getConfirmationPeriod().getHours());
        code.setFacilityId(facilityId);
        code.setRequestId(requestId);
        code.setHash(ServiceUtils.getHash(code.toString()));
        return code;
    }

    private Map<String, String> generateLinksForAuthorities(RequestDTO req, List<String> authorities, User user)
            throws UnsupportedEncodingException, InternalErrorException
    {
        Map<String, String> codeMap = generateCodesForAuthorities(req, authorities, user);
        Map<String, String> linksMap = new HashMap<>();
        for (Map.Entry<String, String> entry : codeMap.entrySet()) {
            String code = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString());
            String link = approvalsProperties.getAuthoritiesEndpoint()
                    .concat("?code=").concat(code);
            linksMap.put(entry.getKey(), link);
        }
        return linksMap;
    }

    private boolean hasCorrectStatus(RequestStatus status, RequestStatus[] allowedStatuses) {
        for (RequestStatus s: allowedStatuses) {
            if (s.equals(status)) {
                return true;
            }
        }

        return false;
    }

    private boolean setUserAsServiceManager(Long facilityId, Long adminsGroupId, Long requesterId)
            throws PerunUnknownException, PerunConnectionException
    {
        boolean adminSet = perunAdapter.addGroupAsAdmins(facilityId, adminsGroupId);
        if (!adminSet) {
            log.error("Could not set group {} as managers for facility {}", adminsGroupId, facilityId);
            return false;
        }
        Long memberId = perunAdapter.getMemberIdByUser(applicationProperties.getSpManagersVoId(), requesterId);
        if (memberId == null) {
            log.error("Could not set user {} as manager in group {}, user does not have member",
                    requesterId, adminsGroupId);
            return false;
        }
        return perunAdapter.addMemberToGroup(adminsGroupId, memberId);
    }

    private Long createAdminsGroup(String perunFacilityName)
            throws PerunUnknownException, PerunConnectionException, InternalErrorException {
        Group adminsGroup = new Group(perunFacilityName, perunFacilityName,
                "Administrators of SP - " + perunFacilityName,
                applicationProperties.getSpManagersParentGroupId(),
                applicationProperties.getSpManagersVoId());
        adminsGroup = perunAdapter.createGroup(adminsGroup.getParentGroupId(), adminsGroup);
        if (adminsGroup == null || adminsGroup.getId() == null) {
            throw new InternalErrorException("Could not create admins group");
        }
        return adminsGroup.getId();
    }

    private boolean isOidcCredentialAttr(PerunAttribute a) {
        return isOidcCredentialAttr(a, true);
    }

    private boolean isOidcCredentialAttr(PerunAttribute a, boolean filterClientId) {
        if (attributesProperties.getNames().getOidcClientSecret().equalsIgnoreCase(a.getFullName())) {
            return true;
        }
        return filterClientId && attributesProperties.getNames().getOidcClientId().equalsIgnoreCase(a.getFullName());
    }

    private void insertRegisterServiceReqCreatedAuditLog(Long requestId, Long actorId, String actorName) {
        insertAuditLog(requestId, actorId, actorName, "Service registration request created");
    }

    private void insertUpdateServiceReqCreatedAuditLog(Long requestId, Long actorId, String actorName) {
        insertAuditLog(requestId, actorId, actorName, "Update settings request created");
    }

    private void insertRemoveServiceReqCreatedAuditLog(Long requestId, Long actorId, String actorName) {
        insertAuditLog(requestId, actorId, actorName, "Remove service request created");
    }

    private void insertTransferServiceReqCreatedAuditLog(Long requestId, Long actorId, String actorName) {
        insertAuditLog(requestId, actorId, actorName, "Transfer to production request created");
    }

    private void insertApproveRequestAuditLog(Long requestId, Long actorId, String actorName) {
        insertAuditLog(requestId, actorId, actorName, "Approved by administrators");
    }

    private void insertRejectRequestAuditLog(Long requestId, Long actorId, String actorName) {
        insertAuditLog(requestId, actorId, actorName, "Rejected by administrators");
    }

    private void insertCancelRequestAuditLog(Long requestId, Long actorId, String actorName) {
        insertAuditLog(requestId, actorId, actorName, "Canceled by the requester");
    }

    private void insertChangesRequestedRequestAuditLog(Long requestId, Long actorId, String actorName) {
        insertAuditLog(requestId, actorId, actorName, "Administrator requested changes in proposed settings");
    }

    private void insertUpdatedRequestAuditLog(Long requestId, Long actorId, String actorName) {
        insertAuditLog(requestId, actorId, actorName, "Updated by requester");
    }

}
