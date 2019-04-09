package cz.metacentrum.perun.spRegistration.service.impl;

import cz.metacentrum.perun.spRegistration.persistence.Utils;
import cz.metacentrum.perun.spRegistration.persistence.configs.AppConfig;
import cz.metacentrum.perun.spRegistration.persistence.configs.MitreIdAttrsConfig;
import cz.metacentrum.perun.spRegistration.persistence.connectors.MitreIdConnector;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestStatus;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.MitreIDApiException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.RPCException;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestManager;
import cz.metacentrum.perun.spRegistration.persistence.models.Facility;
import cz.metacentrum.perun.spRegistration.persistence.models.MitreIdResponse;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import cz.metacentrum.perun.spRegistration.persistence.models.RequestSignature;
import cz.metacentrum.perun.spRegistration.persistence.connectors.PerunConnector;
import cz.metacentrum.perun.spRegistration.service.AdminCommandsService;
import cz.metacentrum.perun.spRegistration.service.ServiceUtils;
import cz.metacentrum.perun.spRegistration.service.exceptions.CannotChangeStatusException;
import cz.metacentrum.perun.spRegistration.service.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.service.exceptions.UnauthorizedActionException;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Implementation of AdminCommandsService.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Service("adminService")
public class AdminCommandsCommandsServiceImpl implements AdminCommandsService {

	private static final Logger log = LoggerFactory.getLogger(AdminCommandsCommandsServiceImpl.class);

	private final RequestManager requestManager;
	private final PerunConnector perunConnector;
	private final AppConfig appConfig;
	private final Properties messagesProperties;
	private final String adminsAttr;
	private final MitreIdAttrsConfig mitreIdAttrsConfig;
	private final MitreIdConnector mitreIdConnector;

	@Autowired
	public AdminCommandsCommandsServiceImpl(RequestManager requestManager, PerunConnector perunConnector,
											AppConfig appConfig, Properties messagesProperties,
											MitreIdAttrsConfig mitreIdAttrsConfig, MitreIdConnector mitreIdConnector) {
		this.requestManager = requestManager;
		this.perunConnector = perunConnector;
		this.appConfig = appConfig;
		this.messagesProperties = messagesProperties;
		this.adminsAttr = appConfig.getAdminsAttr();
		this.mitreIdAttrsConfig = mitreIdAttrsConfig;
		this.mitreIdConnector = mitreIdConnector;
	}

	@Override
	public boolean approveRequest(Long requestId, Long userId)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException, RPCException, MitreIDApiException {

		log.debug("approveRequest(requestId: {}, userId: {})", requestId, userId);
		if (requestId == null || userId == null) {
			log.error("Illegal input - requestId: {}, userId: {}", requestId, userId);
			throw new IllegalArgumentException("Illegal input - requestId: " + requestId + ", userId: " + userId);
		} else if (! appConfig.isAdmin(userId)) {
			log.error("User is not authorized to approve request");
			throw new UnauthorizedActionException("User is not authorized to approve request");
		}

		Request request = requestManager.getRequestById(requestId);
		if (request == null) {
			log.error("Could not fetch request with ID: {} from database", requestId);
			throw new InternalErrorException("Could not fetch request with ID: " + requestId + " from database");
		} else if (! RequestStatus.WFA.equals(request.getStatus())) {
			log.error("Cannot approve request, request not marked as WAITING_FOR_APPROVAL");
			throw new CannotChangeStatusException("Cannot approve request, request not marked as WAITING_FOR_APPROVAL");
		}

		if (! finishRequestApproved(request)) {
			log.error("Could not finish approving request");
			return false;
		}

		boolean res = Utils.updateRequestAndNotifyUser(requestManager, request, RequestStatus.APPROVED, messagesProperties, adminsAttr);

		log.debug("updateRequestInDbAndNotifyUser() returns: {}", res);
		return res;
	}

	@Override
	public boolean rejectRequest(Long requestId, Long userId, String message)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException {

		log.debug("rejectRequest(requestId: {}, userId: {}, message: {})", requestId, userId, message);
		if (requestId == null || userId == null) {
			log.error("Illegal input - requestId: {}, userId: {}", requestId, userId);
			throw new IllegalArgumentException("Illegal input - requestId: " + requestId + ", userId: " + userId);
		} else if (! appConfig.isAdmin(userId)) {
			log.error("User is not authorized to reject request");
			throw new UnauthorizedActionException("User is not authorized to reject request");
		}

		Request request = requestManager.getRequestById(requestId);
		if (request == null) {
			log.error("Could not fetch request with ID: {} from database", requestId);
			throw new InternalErrorException("Could not fetch request with ID: " + requestId + " from database");
		} else if (! RequestStatus.WFA.equals(request.getStatus())) {
			log.error("Cannot reject request, request not marked as WAITING_FOR_APPROVAL");
			throw new CannotChangeStatusException("Cannot reject request, request not marked as WAITING_FOR_APPROVAL");
		}

		boolean res = Utils.updateRequestAndNotifyUser(requestManager, request, RequestStatus.REJECTED, messagesProperties, adminsAttr);

		log.debug("updateRequestInDbAndNotifyUser() returns: {}", res);
		return res;
	}

	@Override
	public boolean askForChanges(Long requestId, Long userId, List<PerunAttribute> attributes)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException {

		log.debug("askForChanges(requestId: {}, userId: {}, attributes: {})", requestId, userId, attributes);
		if (requestId == null || userId == null || attributes == null) {
			log.error("Illegal input - requestId: {}, userId: {}, attributes: {}", requestId, userId, attributes);
			throw new IllegalArgumentException("Illegal input - requestId: " + requestId + ", userId: " + userId + ", attributes: " + attributes);
		} else if (! appConfig.isAdmin(userId)) {
			log.error("User is not authorized to ask for changes");
			throw new UnauthorizedActionException("User is not authorized to ask for changes");
		}

		Request request = requestManager.getRequestById(requestId);
		if (request == null) {
			log.error("Could not fetch request with ID: {} from database", requestId);
			throw new InternalErrorException("Could not fetch request with ID: " + requestId + " from database");
		} else if (! RequestStatus.WFA.equals(request.getStatus())) {
			log.error("Cannot ask for changes, request not marked as WAITING_FOR_APPROVAL");
			throw new CannotChangeStatusException("Cannot ask for changes, request not marked as WAITING_FOR_APPROVAL");
		}

		Map<String, PerunAttribute> convertedAttributes = ServiceUtils.transformListToMap(attributes, appConfig);
		request.setAttributes(convertedAttributes);
		boolean res = Utils.updateRequestAndNotifyUser(requestManager, request, RequestStatus.WFC, messagesProperties, adminsAttr);

		log.debug("askForChanges returns: {}", res);
		return res;
	}

	@Override
	public List<RequestSignature> getApprovalsOfProductionTransfer(Long requestId, Long userId) throws UnauthorizedActionException {
		log.debug("getApprovalsOfProductionTransfer(requestId: {}, userId: {})", requestId, userId);
		if (userId == null || requestId == null) {
			log.error("Illegal input - requestId: {}, userId: {} " , requestId, userId);
			throw new IllegalArgumentException("Illegal input - requestId: " + requestId + ", userId: " + userId);
		} else if (! appConfig.isAdmin(userId)) {
			log.error("User is not authorized to view approvals");
			throw new UnauthorizedActionException("User is not authorized to view approvals");
		}

		List<RequestSignature> result = requestManager.getRequestSignatures(requestId);
		log.debug("getApprovalsOfProductionTransfer returns: {}", result);
		return result;
	}

	@Override
	public List<Request> getAllRequests(Long adminId) throws UnauthorizedActionException {
		log.debug("getAllRequests({})", adminId);
		if (adminId == null) {
			log.error("Illegal input - adminId is null");
			throw new IllegalArgumentException("Illegal input - adminId is null");
		} else if (! appConfig.isAdmin(adminId)) {
			log.error("User cannot list all requests, user is not an admin");
			throw new UnauthorizedActionException("User cannot list all requests, user is not an admin");
		}

		List<Request> result = requestManager.getAllRequests();

		log.debug("getAllRequests returns: {}", result);
		return result;
	}

	@Override
	public List<Facility> getAllFacilities(Long adminId) throws UnauthorizedActionException, RPCException {
		log.debug("getAllFacilities({})", adminId);
		if (adminId == null) {
			log.error("Illegal input - adminId is null");
			throw new IllegalArgumentException("Illegal input - adminId is null");
		} else if (! appConfig.isAdmin(adminId)) {
			log.error("User cannot list all facilities, user not an admin");
			throw new UnauthorizedActionException("User cannot list all facilities, user not an admin");
		}

		Map<String, String> params = new HashMap<>();
		params.put(appConfig.getIdpAttribute(), appConfig.getIdpAttributeValue());
		List<Facility> result = perunConnector.getFacilitiesViaSearcher(params);

		log.debug("getAllFacilities returns: {}", result);
		return result;
	}

	private boolean finishRequestApproved(Request request) throws RPCException, InternalErrorException, MitreIDApiException {
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

	private boolean registerNewFacilityToPerun(Request request) throws RPCException, InternalErrorException, MitreIDApiException {
		log.debug("registerNewFacilityToPerun({})", request);

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
		}

		request.setFacilityId(facility.getId());

		log.info("Setting facility attributes");
		boolean result = perunConnector.setFacilityAttributes(request.getFacilityId(), request.getAttributesAsJsonArrayForPerun());
		result = result && perunConnector.addFacilityAdmin(facility.getId(), request.getReqUserId());

		if (isOidc(request)) {
			result = result && setClientIdAndMitreClientId(request);
		}

		log.debug("registerNewFacilityToPerun returns: {}", result);
		return result;
	}

	private boolean updateFacilityInPerun(Request request) throws RPCException, InternalErrorException, MitreIDApiException {
		log.debug("updateFacilityInPerun({})", request);

		Long facilityId = extractFacilityIdFromRequest(request);

		log.debug("Fetching facility with ID: {} from Perun ", facilityId);

		Facility actualFacility = perunConnector.getFacilityById(facilityId);
		if (actualFacility == null) {
			log.error("Facility with ID: {} does not exist in Perun", facilityId);
			throw new InternalErrorException("Facility with ID: " + facilityId + " does not exist in Perun");
		}

		log.info("Setting facility attributes");
		boolean result = perunConnector.setFacilityAttributes(request.getFacilityId(), request.getAttributesAsJsonArrayForPerun());

		String newName = request.getFacilityName();
		String newDesc = request.getFacilityDescription();

		boolean changed = false;
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
			perunConnector.updateFacilityInPerun(actualFacility.toJson());
		}

		log.debug("updating mitreid client");
		PerunAttribute mitreClientId = perunConnector.getFacilityAttribute(facilityId, mitreIdAttrsConfig.getMitreClientIdAttr());
		result = result && mitreIdConnector.updateClient(mitreClientId.valueAsLong(), request.getAttributes());

		log.debug("updateFacilityInPerun returns: {}", result);
		return result;
	}

	private boolean deleteFacilityFromPerun(Request request) throws RPCException, MitreIDApiException {
		log.debug("deleteFacilityFromPerun({})", request);
		Long facilityId = extractFacilityIdFromRequest(request);

		log.info("Removing facility with ID: {} from Perun", facilityId);
		boolean result = perunConnector.deleteFacilityFromPerun(facilityId);
		if (! result) {
			log.error("Facility has not been removed");
		}

		PerunAttribute mitreClientId = perunConnector.getFacilityAttribute(facilityId, mitreIdAttrsConfig.getMitreClientIdAttr());
		result = result && mitreIdConnector.deleteClient(mitreClientId.valueAsLong());

		log.debug("deleteFacilityFromPerun returns: {}", result);
		return result;
	}

	private boolean moveToProduction(Request request) throws RPCException {
		log.debug("requestMoveToProduction({})", request);
		log.info("Updating facility attributes");
		boolean res;
		PerunAttribute testSp = perunConnector.getFacilityAttribute(
				request.getFacilityId(), appConfig.getTestSpAttribute());
		testSp.setValue(false);
		res = perunConnector.setFacilityAttribute(request.getFacilityId(), testSp.toJson());
		PerunAttribute displayOnList = perunConnector.getFacilityAttribute(
				request.getFacilityId(), appConfig.getShowOnServicesListAttribute());
		displayOnList.setValue(true);
		res = res && perunConnector.setFacilityAttribute(request.getFacilityId(), displayOnList.toJson());

		log.debug("requestMoveToProduction returns: {}", res);
		return res;
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

	private boolean isOidc(Request request) {
		return request.getAttributes().containsKey(mitreIdAttrsConfig.getGrantTypesAttrs());
	}

	private boolean setClientIdAndMitreClientId(Request request) throws MitreIDApiException, RPCException {
		log.debug("setClientIdAndMitreClientId({})", request);
		MitreIdResponse response = mitreIdConnector.createClient(request.getAttributes());
		log.debug("mitreid response: {}", response);

		List<String> toFetch = new ArrayList<>();
		toFetch.add(mitreIdAttrsConfig.getClientIdAttr());
		toFetch.add(mitreIdAttrsConfig.getMitreClientIdAttr());

		Map<String, PerunAttribute> attrs = perunConnector.getFacilityAttributes(request.getFacilityId(), toFetch);
		attrs.get(mitreIdAttrsConfig.getClientIdAttr()).setValue(response.getClientId());
		attrs.get(mitreIdAttrsConfig.getMitreClientIdAttr()).setValue(response.getId());

		JSONArray jsonArr = new JSONArray();
		jsonArr.put(attrs.get(mitreIdAttrsConfig.getClientIdAttr()).toJson());
		jsonArr.put(attrs.get(mitreIdAttrsConfig.getMitreClientIdAttr()).toJson());

		boolean res = perunConnector.setFacilityAttributes(request.getFacilityId(), jsonArr);
		log.debug("setClientIdAndMitreClientId returns: {}", res);
		return res;
	}
}
