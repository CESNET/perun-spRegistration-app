package cz.metacentrum.perun.spRegistration.service.impl;

import cz.metacentrum.perun.spRegistration.persistence.Utils;
import cz.metacentrum.perun.spRegistration.persistence.configs.AppConfig;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestStatus;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.RPCException;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestManager;
import cz.metacentrum.perun.spRegistration.persistence.models.Facility;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import cz.metacentrum.perun.spRegistration.persistence.models.RequestSignature;
import cz.metacentrum.perun.spRegistration.persistence.rpc.PerunConnector;
import cz.metacentrum.perun.spRegistration.service.AdminService;
import cz.metacentrum.perun.spRegistration.service.ServiceUtils;
import cz.metacentrum.perun.spRegistration.service.exceptions.CannotChangeStatusException;
import cz.metacentrum.perun.spRegistration.service.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.service.exceptions.UnauthorizedActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Implementation of AdminService.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Service("adminService")
public class AdminServiceImpl implements AdminService {

	private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

	private final RequestManager requestManager;
	private final PerunConnector perunConnector;
	private final AppConfig appConfig;
	private final Properties messagesProperties;
	private final String adminsAttr;

	@Autowired
	public AdminServiceImpl(RequestManager requestManager, PerunConnector perunConnector, AppConfig appConfig, Properties messagesProperties) {
		this.requestManager = requestManager;
		this.perunConnector = perunConnector;
		this.appConfig = appConfig;
		this.messagesProperties = messagesProperties;
		this.adminsAttr = appConfig.getAdminsAttr();
	}

	@Override
	public boolean approveRequest(Long requestId, Long userId)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException, RPCException {

		log.debug("approveRequest(requestId: {}, userId: {})", requestId, userId);
		if (requestId == null || userId == null) {
			log.error("Illegal input - requestId: {}, userId: {}", requestId, userId);
			throw new IllegalArgumentException("Illegal input - requestId: " + requestId + ", userId: " + userId);
		} else if (! appConfig.isAdmin(userId)) {
			log.error("User is not authorized to approve request");
			throw new UnauthorizedActionException("User is not authorized to approve request");
		}

		Request request = requestManager.getRequestByReqId(requestId);
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

		boolean res = Utils.updaterequestAndNotifyUser(requestManager, request, RequestStatus.APPROVED, messagesProperties, adminsAttr);

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

		Request request = requestManager.getRequestByReqId(requestId);
		if (request == null) {
			log.error("Could not fetch request with ID: {} from database", requestId);
			throw new InternalErrorException("Could not fetch request with ID: " + requestId + " from database");
		} else if (! RequestStatus.WFA.equals(request.getStatus())) {
			log.error("Cannot reject request, request not marked as WAITING_FOR_APPROVAL");
			throw new CannotChangeStatusException("Cannot reject request, request not marked as WAITING_FOR_APPROVAL");
		}

		boolean res = Utils.updaterequestAndNotifyUser(requestManager, request, RequestStatus.REJECTED, messagesProperties, adminsAttr);

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

		Request request = requestManager.getRequestByReqId(requestId);
		if (request == null) {
			log.error("Could not fetch request with ID: {} from database", requestId);
			throw new InternalErrorException("Could not fetch request with ID: " + requestId + " from database");
		} else if (! RequestStatus.WFA.equals(request.getStatus())) {
			log.error("Cannot ask for changes, request not marked as WAITING_FOR_APPROVAL");
			throw new CannotChangeStatusException("Cannot ask for changes, request not marked as WAITING_FOR_APPROVAL");
		}

		Map<String, PerunAttribute> convertedAttributes = ServiceUtils.transformListToMap(attributes, appConfig);
		request.setAttributes(convertedAttributes);
		boolean res = Utils.updaterequestAndNotifyUser(requestManager, request, RequestStatus.WFC, messagesProperties, adminsAttr);

		log.debug("askForChanges returns: {}", res);
		return res;
	}

	@Override
	public boolean approveTransferToProduction(Long requestId, Long userId) throws InternalErrorException, UnauthorizedActionException, RPCException {
		log.debug("approveTransferToProduction(requestId: {}, userId: {})", requestId, userId);
		if (requestId == null || userId == null) {
			log.error("Illegal input - requestId: {}, userId: {}", requestId, userId);
			throw new IllegalArgumentException("Illegal input - requestId: " + requestId + ", userId: " + userId);
		} else if (! appConfig.isAdmin(userId)) {
			log.error("User cannot approve transfer, user not an admin");
			throw new UnauthorizedActionException("User cannot approve transfer, user not an admin");
		}

		Request request = requestManager.getRequestByReqId(requestId);
		if (request == null) {
			log.error("Could not fetch request with ID: {} from database", requestId);
			throw new InternalErrorException("Could not fetch request with ID: " + requestId + " from database");
		}

		boolean res = Utils.updaterequestAndNotifyUser(requestManager, request, RequestStatus.APPROVED, messagesProperties, adminsAttr);
		finishRequestApproved(request);

		log.debug("approveTransferToProduction returns: {}", res);
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

	@Override
	public boolean addAdmins(Long userId, Long facilityId, List<Long> admins) throws UnauthorizedActionException, RPCException {
		log.debug("addAdmins(userId: {}, facilityId: {}, admins: {})", userId, facilityId, admins);
		if (userId == null || facilityId == null || admins == null) {
			log.error("Illegal input - userId: {}, facilityId: {}, admins: {}", userId, facilityId, admins);
			throw new IllegalArgumentException("Illegal input - userId: " + userId + ", facilityId: " + facilityId + ", admins: " + admins);
		} else if (! appConfig.isAdmin(userId)) {
			log.error("User is not authorized to add facility admins");
			throw new UnauthorizedActionException("User is not authorized to add facility admins");
		}

		log.info("Add admins");
		boolean result = true;
		for (Long id: admins) {
			boolean partial = perunConnector.addFacilityAdmin(facilityId, id);
			log.debug("adding admin with id: {} succeeded: {}", id, partial);
			result = result && partial;
		}
		log.info("Admins were added - all calls successful: {}", result);

		log.debug("addAdmins returns: {}", result);
		return result;
	}

	@Override
	public boolean removeAdmins(Long userId, Long facilityId, List<Long> admins) throws UnauthorizedActionException, RPCException {
		log.debug("removeAdmins(userId: {}, facilityId: {}, admins: {})", userId, facilityId, admins);
		if (userId == null || facilityId == null || admins == null) {
			log.error("Illegal input - userId: {}, facilityId: {}, admins: {}", userId, facilityId, admins);
			throw new IllegalArgumentException("Illegal input - userId: " + userId + ", facilityId: " + facilityId + ", admins: " + admins);
		} else if (! appConfig.isAdmin(userId)) {
			log.error("User is not authorized to remove facility admins");
			throw new UnauthorizedActionException("User is not authorized to remove facility admins");
		}

		log.debug("Removing admins");
		boolean result = true;
		for (Long id: admins) {
			boolean partial = perunConnector.removeFacilityAdmin(facilityId, id);
			log.debug("removing admin with id: {} succeeded: {}", id, partial);
			result = result && partial;
		}
		log.debug("Removing admins finished - all calls successful: {}", result);

		log.debug("removeAdmins returns: {}", result);
		return result;
	}

	private boolean finishRequestApproved(Request request) throws RPCException, InternalErrorException {
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

	private boolean registerNewFacilityToPerun(Request request) throws RPCException, InternalErrorException {
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
		facility = perunConnector.createFacilityInPerun(facility.toJsonString());

		if (facility == null) {
			log.error("Creating facility in Perun failed");
			throw new InternalErrorException("Creating facility in Perun failed");
		}

		request.setFacilityId(facility.getId());

		log.info("Setting facility attributes");
		boolean result = perunConnector.setFacilityAttributes(request.getFacilityId(), request.getAttributesAsJsonForPerun());

		log.debug("registerNewFacilityToPerun returns: {}", result);
		return result;
	}

	private boolean updateFacilityInPerun(Request request) throws RPCException, InternalErrorException {
		log.debug("updateFacilityInPerun({})", request);

		Long facilityId = extractFacilityIdFromRequest(request);

		log.debug("Fetching facility with ID: {} from Perun ", facilityId);
		Facility actualFacility = perunConnector.getFacilityById(facilityId);
		if (actualFacility == null) {
			log.error("Facility with ID: {} does not exist in Perun", facilityId);
			throw new InternalErrorException("Facility with ID: " + facilityId + " does not exist in Perun");
		}

		log.info("Setting facility attributes");
		boolean result = perunConnector.setFacilityAttributes(request.getFacilityId(), request.getAttributesAsJsonForPerun());

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
			perunConnector.updateFacilityInPerun(actualFacility.toJsonString());
		}

		log.debug("updateFacilityInPerun returns: {}", result);
		return result;
	}

	private boolean deleteFacilityFromPerun(Request request) throws RPCException {
		log.debug("deleteFacilityFromPerun({})", request);
		Long facilityId = extractFacilityIdFromRequest(request);

		log.info("Removing facility with ID: {} from Perun", facilityId);
		boolean result = perunConnector.deleteFacilityFromPerun(facilityId);
		if (! result) {
			log.error("Facility has not been removed");
		}

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
		res = perunConnector.setFacilityAttribute(request.getFacilityId(), testSp.toJsonForPerun().toString());
		PerunAttribute displayOnList = perunConnector.getFacilityAttribute(
				request.getFacilityId(), appConfig.getShowOnServicesListAttribute());
		displayOnList.setValue(true);
		res = res && perunConnector.setFacilityAttribute(request.getFacilityId(), displayOnList.toJsonForPerun().toString());

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
}
