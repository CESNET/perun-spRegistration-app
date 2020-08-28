package cz.metacentrum.perun.spRegistration.persistence.managers.impl;

import cz.metacentrum.perun.spRegistration.Utils;
import cz.metacentrum.perun.spRegistration.common.configs.ApplicationBeans;
import cz.metacentrum.perun.spRegistration.common.configs.Config;
import cz.metacentrum.perun.spRegistration.common.enums.RequestAction;
import cz.metacentrum.perun.spRegistration.common.enums.RequestStatus;
import cz.metacentrum.perun.spRegistration.common.exceptions.ActiveRequestExistsException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.models.Request;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestManager;
import cz.metacentrum.perun.spRegistration.persistence.mappers.RequestMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Implementation of Request Manager. Works with DB and Request objects.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@Component("requestManager")
@EnableTransactionManagement
@Slf4j
public class RequestManagerImpl implements RequestManager {

	private static final String REQUESTS_TABLE = "requests";

	private final RequestMapper REQUEST_MAPPER;
	private final NamedParameterJdbcTemplate jdbcTemplate;
	private final ApplicationBeans applicationBeans;

	@Autowired
	public RequestManagerImpl(Config config,
							  NamedParameterJdbcTemplate jdbcTemplate,
							  ApplicationBeans applicationBeans)
	{
		REQUEST_MAPPER = new RequestMapper(config, applicationBeans);
		this.jdbcTemplate = jdbcTemplate;
		this.applicationBeans = applicationBeans;
	}

	@Override
	@Transactional
	public Long createRequest(Request request) throws InternalErrorException, ActiveRequestExistsException {
		log.trace("createRequest({})", request);

		if (Utils.checkParamsInvalid(request)) {
			log.error("Wrong parameters passed: (request : {})", request);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		if (request.getFacilityId() != null) {
			Long activeRequestId = getActiveRequestIdByFacilityId(request.getFacilityId());
			if (activeRequestId != null) {
				log.error("Active requests already exist for facilityId: {}", request.getFacilityId());
				throw new ActiveRequestExistsException();
			}
		}

		String query = new StringJoiner(" ")
				.add("INSERT INTO").add(REQUESTS_TABLE)
				.add("(facility_id, status, action, requesting_user_id, attributes, modified_by)")
				.add("VALUES (:fac_id, :status, :action, :req_user_id, :attributes, :modified_by)")
				.toString();

		KeyHolder key = new GeneratedKeyHolder();
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("fac_id", request.getFacilityId());
		params.addValue("status", request.getStatus().getAsInt());
		params.addValue("action", request.getAction().getAsInt());
		params.addValue("req_user_id", request.getReqUserId());
		params.addValue("attributes", request.getAttributesAsJsonForDb(applicationBeans));
		params.addValue("modified_by", request.getModifiedBy());

		int updatedCount = jdbcTemplate.update(query, params, key, new String[] { "id" });

		if (updatedCount == 0) {
			log.error("Zero requests have been inserted");
			throw new InternalErrorException("Zero requests have been inserted");
		} else if (updatedCount > 1) {
			log.error("Only one request should have been inserted");
			throw new InternalErrorException("Only one request should have been inserted");
		}

		Number generatedKey = key.getKey();
		Long generatedId = null;
		if (generatedKey != null) {
			generatedId = generatedKey.longValue();
		}

		log.trace("createRequest() returns: {}", generatedId);
		return generatedId;
	}

	@Override
	@Transactional
	public boolean updateRequest(Request request) throws InternalErrorException {
		log.trace("updateRequest({})", request);

		if (Utils.checkParamsInvalid(request)) {
			log.error("Wrong parameters passed: (request: {})", request);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}
		
		String query = new StringJoiner(" ")
				.add("UPDATE").add(REQUESTS_TABLE)
				.add("SET facility_id = :fac_id, status = :status, action = :action, requesting_user_id = :req_user_id,")
				.add("attributes = :attributes, modified_by = :modified_by, modified_at = NOW()")
				.add("WHERE id = :req_id")
				.toString();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("fac_id", request.getFacilityId());
		params.addValue("status", request.getStatus().getAsInt());
		params.addValue("action", request.getAction().getAsInt());
		params.addValue("req_user_id", request.getReqUserId());
		params.addValue("attributes", request.getAttributesAsJsonForDb(applicationBeans));
		params.addValue("modified_by", request.getModifiedBy());
		params.addValue("req_id", request.getReqId());

		int updatedCount = jdbcTemplate.update(query, params);

		if (updatedCount == 0) {
			log.error("Zero requests have been updated");
			throw new InternalErrorException("Zero requests have been updated");
		} else if (updatedCount > 1) {
			log.error("Only one request should have been updated");
			throw new InternalErrorException("Only one request should have been updated");
		}

		log.trace("updateRequest returns: true");
		return true;
	}

	@Override
	@Transactional
	public boolean deleteRequest(Long reqId) throws InternalErrorException {
		log.trace("deleteRequest({})", reqId);

		if (Utils.checkParamsInvalid(reqId)) {
			log.error("Wrong parameters passed: (reqId: {})", reqId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		String query = new StringJoiner(" ")
				.add("DELETE FROM").add(REQUESTS_TABLE)
				.add("WHERE id = :req_id")
				.toString();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("req_id", reqId);

		int updatedCount = jdbcTemplate.update(query, params);

		if (updatedCount == 0) {
			log.error("Zero requests have been deleted");
			throw new InternalErrorException("Zero requests have been deleted");
		} else if (updatedCount > 1) {
			log.error("Only one request should have been deleted");
			throw new InternalErrorException("Only one request should have been deleted");
		}

		log.trace("deleteRequest returns: true");
		return true;
	}

	@Override
	@Transactional
	public Request getRequestById(Long reqId) {
		log.trace("getRequestById({})", reqId);

		if (Utils.checkParamsInvalid(reqId)) {
			log.error("Wrong parameters passed: (reqId: {})", reqId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}
		
		String query = new StringJoiner(" ")
				.add("SELECT * FROM").add(REQUESTS_TABLE)
				.add("WHERE id = :req_id")
				.toString();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("req_id", reqId);

		Request foundRequest = jdbcTemplate.queryForObject(query, params, REQUEST_MAPPER);

		log.trace("getRequestById returns: {}", foundRequest);
		return foundRequest;
	}

	@Override
	@Transactional
	public List<Request> getAllRequests() {
		log.trace("getAllRequests()");

		String query = new StringJoiner(" ")
				.add("SELECT * FROM").add(REQUESTS_TABLE)
				.toString();

		List<Request> foundRequests = jdbcTemplate.query(query, REQUEST_MAPPER);

		log.trace("getAllRequests returns: {}", foundRequests);
		return foundRequests;
	}

	@Override
	@Transactional
	public List<Request> getAllRequestsByUserId(Long userId) {
		log.trace("getAllRequestsByUserId({})", userId);

		if (Utils.checkParamsInvalid(userId)) {
			log.error("Wrong parameters passed: (userId: {})", userId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}
		
		String query = new StringJoiner(" ")
				.add("SELECT * FROM").add(REQUESTS_TABLE)
				.add("WHERE requesting_user_id = :req_user_id")
				.toString();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("req_user_id", userId);

		List<Request> foundRequests = jdbcTemplate.query(query, params, REQUEST_MAPPER);

		log.trace("getAllRequestsByUserId returns: {}", foundRequests);
		return foundRequests;
	}

	@Override
	@Transactional
	public List<Request> getAllRequestsByStatus(RequestStatus status) {
		log.trace("getAllRequestsByStatus({})", status);

		if (Utils.checkParamsInvalid(status)) {
			log.error("Wrong parameters passed: (status: {})", status);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		String query = new StringJoiner(" ")
				.add("SELECT * FROM").add(REQUESTS_TABLE)
				.add("WHERE status = :status")
				.toString();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("status", status.getAsInt());

		List<Request> foundRequests = jdbcTemplate.query(query, params, REQUEST_MAPPER);

		log.trace("getAllRequestsByStatus returns: {}", foundRequests);
		return foundRequests;
	}

	@Override
	@Transactional
	public List<Request> getAllRequestsByAction(RequestAction action) {
		log.trace("getAllRequestsByAction({})", action);

		if (Utils.checkParamsInvalid(action)) {
			log.error("Wrong parameters passed: (action: {})", action);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		String query = new StringJoiner(" ")
				.add("SELECT * FROM").add(REQUESTS_TABLE)
				.add("WHERE action = :action")
				.toString();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("action", action.getAsInt());

		List<Request> foundRequests = jdbcTemplate.query(query, params, REQUEST_MAPPER);

		log.trace("getAllRequestsByAction returns: {}", foundRequests);
		return foundRequests;
	}

	@Override
	@Transactional
	public List<Request> getAllRequestsByFacilityId(Long facilityId) {
		log.trace("getAllRequestsByFacilityId({})", facilityId);

		if (Utils.checkParamsInvalid(facilityId)) {
			log.error("Wrong parameters passed: (facilityId: {})", facilityId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		String query = new StringJoiner(" ")
				.add("SELECT * FROM").add(REQUESTS_TABLE)
				.add("WHERE facility_id = :fac_id")
				.toString();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("fac_id", facilityId);

		List<Request> foundRequests = jdbcTemplate.query(query, params, REQUEST_MAPPER);

		log.trace("getAllRequestsByFacilityId returns: {}", foundRequests);
		return foundRequests;
	}

	@Override
	@Transactional
	public List<Request> getAllRequestsByFacilityIds(Set<Long> facilityIds) {
		log.trace("getAllRequestsByFacilityIds({})", facilityIds);

		if (Utils.checkParamsInvalid(facilityIds) || facilityIds.isEmpty()) {
			log.error("Wrong parameters passed: (facilityIds: {})", facilityIds);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		String query = new StringJoiner(" ")
				.add("SELECT * FROM").add(REQUESTS_TABLE)
				.add("WHERE facility_id IN (:ids)")
				.toString();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("ids", new ArrayList<>(facilityIds));

		List<Request> foundRequests = jdbcTemplate.query(query, params, REQUEST_MAPPER);

		log.trace("getAllRequestsByFacilityIds returns: {}", foundRequests);
		return foundRequests;
	}

	@Override
	@Transactional
	public Long getActiveRequestIdByFacilityId(Long facilityId) throws InternalErrorException {
		log.trace("getActiveRequestIdByFacilityId({})", facilityId);

		if (Utils.checkParamsInvalid(facilityId)) {
			log.error("Wrong parameters passed: (facilityId: {})", facilityId);
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		String query = new StringJoiner(" ")
				.add("SELECT id FROM").add(REQUESTS_TABLE)
				.add("WHERE facility_id = :fac_id AND (status = :status1 OR status = :status2)")
				.toString();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("fac_id", facilityId);
		params.addValue("status1", RequestStatus.WAITING_FOR_CHANGES.getAsInt());
		params.addValue("status2", RequestStatus.WAITING_FOR_APPROVAL.getAsInt());

		Long activeRequestId;
		try {
			activeRequestId = jdbcTemplate.queryForObject(query, params, Long.class);
		} catch (EmptyResultDataAccessException e) {
			activeRequestId = null;
		} catch (IncorrectResultSizeDataAccessException e) {
			log.error("Two active requests for facility {} found", facilityId);
			throw new InternalErrorException("Two active requests for facility #" + facilityId + " found", e);
		}

		log.trace("getActiveRequestIdByFacilityId returns: {}", activeRequestId);
		return activeRequestId;
	}

}
