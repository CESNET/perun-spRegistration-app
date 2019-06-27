package cz.metacentrum.perun.spRegistration.service.impl;

import cz.metacentrum.perun.spRegistration.persistence.configs.AppConfig;
import cz.metacentrum.perun.spRegistration.persistence.configs.MitreIdAttrsConfig;
import cz.metacentrum.perun.spRegistration.persistence.connectors.MitreIdConnector;
import cz.metacentrum.perun.spRegistration.persistence.connectors.PerunConnector;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestStatus;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.ConnectorException;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestManager;
import cz.metacentrum.perun.spRegistration.persistence.models.Facility;
import cz.metacentrum.perun.spRegistration.persistence.models.MitreIdResponse;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import cz.metacentrum.perun.spRegistration.service.AdminCommandsService;
import cz.metacentrum.perun.spRegistration.service.Mails;
import cz.metacentrum.perun.spRegistration.service.ServiceUtils;
import cz.metacentrum.perun.spRegistration.service.exceptions.CannotChangeStatusException;
import cz.metacentrum.perun.spRegistration.service.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.service.exceptions.UnauthorizedActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Implementation of AdminCommandsService.
 *
 * @author Dominik Frantisek Bucik &lt;bucik@ics.muni.cz&gt;
 */
@Service("adminService")
public class AdminCommandsServiceImpl implements AdminCommandsService {

	private static final Logger log = LoggerFactory.getLogger(AdminCommandsServiceImpl.class);

	private final RequestManager requestManager;
	private final PerunConnector perunConnector;
	private final AppConfig appConfig;
	private final Properties messagesProperties;
	private final MitreIdAttrsConfig mitreIdAttrsConfig;
	private final MitreIdConnector mitreIdConnector;

	@Autowired
	public AdminCommandsServiceImpl(RequestManager requestManager, PerunConnector perunConnector,
									AppConfig appConfig, Properties messagesProperties,
									MitreIdAttrsConfig mitreIdAttrsConfig, MitreIdConnector mitreIdConnector) {
		this.requestManager = requestManager;
		this.perunConnector = perunConnector;
		this.appConfig = appConfig;
		this.messagesProperties = messagesProperties;
		this.mitreIdAttrsConfig = mitreIdAttrsConfig;
		this.mitreIdConnector = mitreIdConnector;
	}

	@Override
	public boolean approveRequest(Long requestId, Long userId)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException, ConnectorException {
		log.trace("approveRequest(requestId: {}, userId: {})", requestId, userId);
		if (requestId == null || userId == null) {
			log.error("Illegal input - requestId: {}, userId: {}", requestId, userId);
			throw new IllegalArgumentException("Illegal input - requestId: " + requestId + ", userId: " + userId);
		} else if (! appConfig.isAppAdmin(userId)) {
			log.error("User is not authorized to approve request");
			throw new UnauthorizedActionException("User is not authorized to approve request");
		}

		Request request = requestManager.getRequestById(requestId);
		if (request == null) {
			log.error("Could not fetch request with ID: {} from database", requestId);
			throw new InternalErrorException("Could not fetch request with ID: " + requestId + " from database");
		} else if (! RequestStatus.WAITING_FOR_APPROVAL.equals(request.getStatus())) {
			log.error("Cannot approve request, request not marked as WAITING_FOR_APPROVAL");
			throw new CannotChangeStatusException("Cannot approve request, request not marked as WAITING_FOR_APPROVAL");
		}

		boolean requestProcessed = processApprovedRequest(request);

		request.setStatus(RequestStatus.APPROVED);
		request.setModifiedAt(new Timestamp(System.currentTimeMillis()));
		boolean requestUpdated = requestManager.updateRequest(request);

		boolean notificationSent = Mails.requestStatusUpdateUserNotify(request.getReqId(), RequestStatus.APPROVED,
				request.getAdminContact(appConfig.getAdminsAttributeName()), messagesProperties);

		boolean successful = (requestProcessed && requestUpdated && notificationSent);

		if (! successful) {
			log.error("some operations failed: requestProcessed: {}, requestUpdated: {}, notificationSent: {} for request: {}",
					requestProcessed, requestUpdated, notificationSent, request);
		} else {
			log.info("Request processed, request updated, notification sent");
		}

		log.trace("approveRequest() returns: {}", successful);
		return successful;
	}

	@Override
	public boolean rejectRequest(Long requestId, Long userId, String message)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException {

		log.trace("rejectRequest(requestId: {}, userId: {}, message: {})", requestId, userId, message);
		if (requestId == null || userId == null) {
			log.error("Illegal input - requestId: {}, userId: {}", requestId, userId);
			throw new IllegalArgumentException("Illegal input - requestId: " + requestId + ", userId: " + userId);
		} else if (! appConfig.isAppAdmin(userId)) {
			log.error("User is not authorized to reject request");
			throw new UnauthorizedActionException("User is not authorized to reject request");
		}

		Request request = requestManager.getRequestById(requestId);
		if (request == null) {
			log.error("Could not fetch request with ID: {} from database", requestId);
			throw new InternalErrorException("Could not fetch request with ID: " + requestId + " from database");
		} else if (! RequestStatus.WAITING_FOR_APPROVAL.equals(request.getStatus())) {
			log.error("Cannot reject request, request not marked as WAITING_FOR_APPROVAL");
			throw new CannotChangeStatusException("Cannot reject request, request not marked as WAITING_FOR_APPROVAL");
		}

		request.setStatus(RequestStatus.REJECTED);
		request.setModifiedAt(new Timestamp(System.currentTimeMillis()));

		log.debug("updatingRequest");
		boolean requestUpdated = requestManager.updateRequest(request);

		log.debug("sendingNotification");
		boolean notificationSent = Mails.requestStatusUpdateUserNotify(request.getReqId(), RequestStatus.REJECTED,
				request.getAdminContact(appConfig.getAdminsAttributeName()), messagesProperties);

		boolean successful = (requestUpdated && notificationSent);

		if (! successful) {
			log.error("some operations failed: requestUpdated: {}, notificationSent: {} for request: {}",
					requestUpdated, notificationSent, request);
		} else {
			log.info("Request updated, notification sent");
		}

		log.trace("rejectRequest() returns: {}", successful);
		return successful;
	}

	@Override
	public boolean askForChanges(Long requestId, Long userId, List<PerunAttribute> attributes)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException {

		log.trace("askForChanges(requestId: {}, userId: {}, attributes: {})", requestId, userId, attributes);
		if (requestId == null || userId == null || attributes == null) {
			log.error("Illegal input - requestId: {}, userId: {}, attributes: {}", requestId, userId, attributes);
			throw new IllegalArgumentException("Illegal input - requestId: " + requestId + ", userId: " + userId + ", attributes: " + attributes);
		} else if (! appConfig.isAppAdmin(userId)) {
			log.error("User is not authorized to ask for changes");
			throw new UnauthorizedActionException("User is not authorized to ask for changes");
		}

		Request request = requestManager.getRequestById(requestId);
		if (request == null) {
			log.error("Could not fetch request with ID: {} from database", requestId);
			throw new InternalErrorException("Could not fetch request with ID: " + requestId + " from database");
		} else if (! RequestStatus.WAITING_FOR_APPROVAL.equals(request.getStatus())) {
			log.error("Cannot ask for changes, request not marked as WAITING_FOR_APPROVAL");
			throw new CannotChangeStatusException("Cannot ask for changes, request not marked as WAITING_FOR_APPROVAL");
		}

		Map<String, PerunAttribute> convertedAttributes = ServiceUtils.transformListToMapAttrs(attributes, appConfig);
		request.updateAttributes(convertedAttributes, false);

		request.setStatus(RequestStatus.WAITING_FOR_CHANGES);
		request.setModifiedAt(new Timestamp(System.currentTimeMillis()));

		log.debug("updatingRequest");
		boolean requestUpdated = requestManager.updateRequest(request);

		log.debug("sendingNotification");
		boolean notificationSent = Mails.requestStatusUpdateUserNotify(request.getReqId(), RequestStatus.WAITING_FOR_CHANGES,
				request.getAdminContact(appConfig.getAdminsAttributeName()), messagesProperties);

		boolean successful = (requestUpdated && notificationSent);

		if (! successful) {
			log.error("some operations failed: requestUpdated: {}, notificationSent: {} for request: {}",
					requestUpdated, notificationSent, request);
		} else {
			log.info("Request updated, notification sent");
		}

		log.trace("askForChanges() returns: {}", successful);
		return successful;
	}

	@Override
	public List<Request> getAllRequests(Long userId) throws UnauthorizedActionException {
		log.trace("getAllRequests({})", userId);
		if (userId == null) {
			log.error("Illegal input - userId is null");
			throw new IllegalArgumentException("Illegal input - userId is null");
		} else if (! appConfig.isAppAdmin(userId)) {
			log.error("User cannot list all requests, user is not an admin");
			throw new UnauthorizedActionException("User cannot list all requests, user is not an admin");
		}

		List<Request> result = requestManager.getAllRequests();

		log.trace("getAllRequests returns: {}", result);
		return result;
	}

	@Override
	public List<Facility> getAllFacilities(Long userId) throws UnauthorizedActionException, ConnectorException {
		log.trace("getAllFacilities({})", userId);
		if (userId == null) {
			log.error("Illegal input - userId is null");
			throw new IllegalArgumentException("Illegal input - userId is null");
		} else if (! appConfig.isAppAdmin(userId)) {
			log.error("User cannot list all facilities, user not an admin");
			throw new UnauthorizedActionException("User cannot list all facilities, user not an admin");
		}

		List<Facility> proxyFacilities = perunConnector.getFacilitiesByProxyIdentifier(appConfig.getProxyIdentifierAttributeName(),
				appConfig.getProxyIdentifierAttributeValue());
		Map<Long, Facility> proxyFacilitiesMap = ServiceUtils.transformListToMapFacilities(proxyFacilities);
		List<Facility> testFacilities = perunConnector.getFacilitiesByAttribute(appConfig.getIsTestSpAttributeName(), "true");
		Map<Long, Facility> testFacilitiesMap = ServiceUtils.transformListToMapFacilities(testFacilities);

		for (Map.Entry<Long, Facility> entry: testFacilitiesMap.entrySet()) {
			Long facId = entry.getKey();
			if (proxyFacilitiesMap.containsKey(facId)) {
				Facility testFacility = proxyFacilitiesMap.get(facId);
				testFacility.setTestEnv(true);
			}
		}

		log.trace("getAllFacilities returns: {}", proxyFacilities);
		return proxyFacilities;
	}

	private boolean processApprovedRequest(Request request) throws InternalErrorException, ConnectorException {
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

	private boolean registerNewFacilityToPerun(Request request) throws InternalErrorException, ConnectorException {
		log.trace("registerNewFacilityToPerun({})", request);

		Facility facility = new Facility(null);
		String newName = request.getFacilityName();
		String newDesc = request.getFacilityDescription();

		if (newName == null || newDesc == null) {
			log.error("Cannot register facility without name and description");
			throw new IllegalArgumentException("Cannot register facility without name and description");
		}

		log.info("Creating facility");
		facility.setName(newName);
		facility.setDescription(newDesc);
		facility = perunConnector.createFacilityInPerun(facility.toJson());

		if (facility == null) {
			log.error("Creating facility in Perun failed");
			throw new InternalErrorException("Creating facility in Perun failed");
		} else {
			log.info("Created facility: {}", facility);
		}
		request.setFacilityId(facility.getId());

		log.info("Setting requesting user as facility admin");
		boolean adminSet = perunConnector.addFacilityAdmin(facility.getId(), request.getReqUserId());

		Map<String, PerunAttribute> additionalAttributes;
		if (ServiceUtils.isOidcRequest(request, appConfig.getEntityIdAttributeName())) {
			log.debug("Creating client in mitreId");
			// TODO: uncomment when connector implemented
			MitreIdResponse mitreResponse = null; // mitreIdConnector.createClient(request.getAttributes());
			additionalAttributes = prepareNewFacilityAttributes(true, false, mitreResponse);
		} else {
			additionalAttributes = prepareNewFacilityAttributes(true, false, null);
		}

		request.updateAttributes(additionalAttributes, true);

		log.info("Setting facility attributes");
		boolean attributesSet = perunConnector.setFacilityAttributes(request.getFacilityId(), request.getAttributesAsJsonArrayForPerun());

		boolean successful = adminSet && attributesSet;
		if (!successful) {
			log.error("Some operations failed - adminSet: {}, attributesSet: {}", adminSet, attributesSet);
		} else {
			log.info("Facility is all set up");
		}

		log.trace("registerNewFacilityToPerun returns: {}", successful);
		return successful;
	}

	private boolean updateFacilityInPerun(Request request) throws InternalErrorException, ConnectorException {
		log.trace("updateFacilityInPerun({})", request);
		Long facilityId = extractFacilityIdFromRequest(request);

		log.debug("Fetching facility with ID: {} from Perun ", facilityId);
		Facility actualFacility = perunConnector.getFacilityById(facilityId);
		if (actualFacility == null) {
			log.error("Facility with ID: {} does not exist in Perun", facilityId);
			throw new InternalErrorException("Facility with ID: " + facilityId + " does not exist in Perun");
		}

		log.info("Updating facility name and description");
		boolean facilityCoreUpdated = updateFacilityNameAndDesc(actualFacility, request);

		log.info("Setting facility attributes");
		boolean attributesSet = perunConnector.setFacilityAttributes(request.getFacilityId(),
				request.getAttributesAsJsonArrayForPerun());

		boolean mitreIdUpdated = true;
		if (ServiceUtils.isOidcRequest(request, appConfig.getEntityIdAttributeName())) {
			log.info("Updating mitreId client");
			PerunAttribute mitreClientId = perunConnector.getFacilityAttribute(facilityId,
					mitreIdAttrsConfig.getMitreClientIdAttr());
			// TODO: uncomment when connector implemented
			//mitreIdUpdated = mitreIdConnector.updateClient(mitreClientId.valueAsLong(), request.getAttributes());
		}

		boolean successful = facilityCoreUpdated && attributesSet && mitreIdUpdated;
		if (!successful) {
			if (ServiceUtils.isOidcRequest(request, appConfig.getEntityIdAttributeName())) {
				log.error("Some operations failed - facilityCoreUpdated: {}, attributesSet: {}, mitreIdUpdated: {}",
						facilityCoreUpdated, attributesSet, mitreIdUpdated);
			} else {
				log.error("Some operations failed - facilityCoreUpdated: {}, attributesSet: {}",
						facilityCoreUpdated, attributesSet);
			}
		}


		log.trace("updateFacilityInPerun returns: {}", successful);
		return successful;
	}

	private boolean deleteFacilityFromPerun(Request request) throws ConnectorException {
		log.trace("deleteFacilityFromPerun({})", request);
		Long facilityId = extractFacilityIdFromRequest(request);

		log.info("Removing facility with ID: {} from Perun", facilityId);
		boolean facilityRemoved = perunConnector.deleteFacilityFromPerun(facilityId);
		boolean mitreIdRemoved = true;

		if (ServiceUtils.isOidcRequest(request, appConfig.getEntityIdAttributeName())) {
			log.info("Removing client from mitreId");
			PerunAttribute mitreClientId = perunConnector.getFacilityAttribute(facilityId,
					mitreIdAttrsConfig.getMitreClientIdAttr());
			// TODO: uncomment when connector implemented
			// mitreIdRemoved = mitreIdConnector.deleteClient(mitreClientId.valueAsLong());
		}

		boolean successful = facilityRemoved && mitreIdRemoved;
		if (!successful) {
			if (ServiceUtils.isOidcRequest(request, appConfig.getEntityIdAttributeName())) {
				log.error("Some operations failed - facilityRemoved: {}, mitreIdRemoved: {}",
						facilityRemoved, mitreIdRemoved);
			} else {
				log.error("Some operations failed - facilityRemoved: {}", facilityRemoved);
			}
		}

		log.trace("deleteFacilityFromPerun returns: {}", successful);
		return successful;
	}

	private boolean moveToProduction(Request request) throws ConnectorException {
		log.trace("requestMoveToProduction({})", request);

		log.info("Updating facility attributes");
		Map<String, PerunAttribute> attributeMap = prepareNewFacilityAttributes(false, true, null);
		request.updateAttributes(attributeMap, true);

		boolean updated = perunConnector.setFacilityAttributes(request.getFacilityId(),
				request.getAttributesAsJsonArrayForPerun());

		log.trace("requestMoveToProduction returns: {}", updated);
		return updated;
	}

	private Long extractFacilityIdFromRequest(Request request) {
		if (request == null) {
			log.error("Request is null");
			throw new IllegalArgumentException("Request is null");
		}

		Long facilityId = request.getFacilityId();
		if (facilityId == null) {
			log.error("Request: {} does not have facilityId", request);
			throw new IllegalArgumentException("Request: " + request.getReqId() + " does not have facilityId");
		}

		return facilityId;
	}

	private Map<String, PerunAttribute> prepareNewFacilityAttributes(boolean testSp, boolean showOnList, MitreIdResponse mitreResponse) {
		log.trace("prepareNewFacilityAttributes(testSp: {}, showOnList: {}, mitreResponse: {})",
				testSp, showOnList, mitreResponse);

		Map<String, PerunAttribute> attributesMap = new HashMap<>();

		PerunAttributeDefinition testSpAttrDef = appConfig.getAttrDefinition(appConfig.getIsTestSpAttributeName());
		PerunAttribute isTestSpAttr = new PerunAttribute(testSpAttrDef, testSp);

		PerunAttributeDefinition showOnListAttrDef = appConfig.getAttrDefinition(appConfig.getShowOnServicesListAttributeName());
		PerunAttribute showOnServicesListAttr = new PerunAttribute(showOnListAttrDef, showOnList);

		if (mitreResponse != null) {
			PerunAttributeDefinition oidcClientIdDef = appConfig.getAttrDefinition(mitreIdAttrsConfig.getClientIdAttr());
			PerunAttribute oidcClientIdAttr = new PerunAttribute(oidcClientIdDef, mitreResponse.getClientId());

			PerunAttributeDefinition mitreClientIdDef = appConfig.getAttrDefinition(mitreIdAttrsConfig.getClientIdAttr());
			PerunAttribute mitreClientIdAttr = new PerunAttribute(mitreClientIdDef, mitreResponse.getId());

			attributesMap.put(oidcClientIdAttr.getFullName(), oidcClientIdAttr);
			attributesMap.put(mitreClientIdAttr.getFullName(), mitreClientIdAttr);
		}
		attributesMap.put(isTestSpAttr.getFullName(), isTestSpAttr);
		attributesMap.put(showOnServicesListAttr.getFullName(), showOnServicesListAttr);

		log.trace("prepareNewFacilityAttributes() returns: {}", attributesMap);
		return attributesMap;
	}

	private boolean updateFacilityNameAndDesc(Facility actualFacility, Request request) throws ConnectorException {
		log.trace("updateFacilityNameAndDesc(actualFacility: {}, request: {})", actualFacility, request);
		String newName = request.getFacilityName();
		String newDesc = request.getFacilityDescription();
		boolean changed = false;
		boolean successful = true;

		if (newName != null && !actualFacility.getName().equals(newName)) {
			log.debug("Update facility name requested");
			actualFacility.setName(newName);
			changed = true;
		}

		if (newDesc != null && !actualFacility.getDescription().equals(newDesc)) {
			log.debug("Update facility description requested");
			actualFacility.setDescription(newDesc);
			changed = true;
		}

		if (changed) {
			log.debug("Updating facility name and/or description");
			successful = null != perunConnector.updateFacilityInPerun(actualFacility.toJson());
		}

		log.trace("updateFacilityNameAndDesc() returns: {}", successful);
		return successful;
	}
}
