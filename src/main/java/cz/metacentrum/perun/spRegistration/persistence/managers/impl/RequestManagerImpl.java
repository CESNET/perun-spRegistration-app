package cz.metacentrum.perun.spRegistration.persistence.managers.impl;

import cz.metacentrum.perun.spRegistration.common.configs.AppBeansContainer;
import cz.metacentrum.perun.spRegistration.common.enums.RequestAction;
import cz.metacentrum.perun.spRegistration.common.enums.RequestStatus;
import cz.metacentrum.perun.spRegistration.common.exceptions.ActiveRequestExistsException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.models.RequestDTO;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestManager;
import cz.metacentrum.perun.spRegistration.persistence.mappers.RequestMapper;
import lombok.NonNull;
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

	public static final String PARAM_ID = "id";
	public static final String PARAM_REQ_ID = "req_id";
	public static final String PARAM_FAC_ID = "fac_id";
	public static final String PARAM_STATUS = "status";
	public static final String PARAM_ACTION = "action";
	public static final String PARAM_REQ_USER_ID = "req_user_id";
	public static final String PARAM_ATTRIBUTES = "attributes";
	public static final String PARAM_MODIFIED_BY = "modified_by";
	public static final String PARAM_IDS = "ids";
	public static final String PARAM_STATUS_WFA = "status_wfa";
	public static final String PARAM_STATUS_WFC = "status_wfc";

	private static final String REQUESTS_TABLE = "requests";


	private final RequestMapper REQUEST_MAPPER;
	private final NamedParameterJdbcTemplate jdbcTemplate;
	private final AppBeansContainer appBeansContainer;

	@Autowired
	public RequestManagerImpl(@NonNull NamedParameterJdbcTemplate jdbcTemplate,
							  @NonNull AppBeansContainer appBeansContainer)
	{
		REQUEST_MAPPER = new RequestMapper(appBeansContainer);
		this.jdbcTemplate = jdbcTemplate;
		this.appBeansContainer = appBeansContainer;
	}

	@Override
	@Transactional
	public Long createRequest(@NonNull RequestDTO request)
			throws InternalErrorException, ActiveRequestExistsException
	{
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
		params.addValue(PARAM_FAC_ID, request.getFacilityId());
		params.addValue(PARAM_STATUS, request.getStatus().getAsInt());
		params.addValue(PARAM_ACTION, request.getAction().getAsInt());
		params.addValue(PARAM_REQ_USER_ID, request.getReqUserId());
		params.addValue(PARAM_ATTRIBUTES, request.getAttributesAsJsonForDb(appBeansContainer));
		params.addValue(PARAM_MODIFIED_BY, request.getModifiedBy());

		int updatedCount = jdbcTemplate.update(query, params, key, new String[] {PARAM_ID});

		if (updatedCount == 0) {
			log.error("Zero requests have been inserted");
			throw new InternalErrorException("Zero requests have been inserted");
		} else if (updatedCount > 1) {
			log.error("Only one request should have been inserted");
			throw new InternalErrorException("Only one request should have been inserted");
		}

		Number generatedKey = key.getKey();
		if (generatedKey == null) {
			throw new InternalErrorException("Did not generate key");
		}

		return generatedKey.longValue();
	}

	@Override
	@Transactional
	public boolean updateRequest(@NonNull RequestDTO request) throws InternalErrorException {
		String query = new StringJoiner(" ")
				.add("UPDATE").add(REQUESTS_TABLE)
				.add("SET facility_id = :fac_id, status = :status, action = :action, requesting_user_id = :req_user_id,")
				.add("attributes = :attributes, modified_by = :modified_by, modified_at = NOW()")
				.add("WHERE id = :req_id")
				.toString();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue(PARAM_FAC_ID, request.getFacilityId());
		params.addValue(PARAM_STATUS, request.getStatus().getAsInt());
		params.addValue(PARAM_ACTION, request.getAction().getAsInt());
		params.addValue(PARAM_REQ_USER_ID, request.getReqUserId());
		params.addValue(PARAM_ATTRIBUTES, request.getAttributesAsJsonForDb(appBeansContainer));
		params.addValue(PARAM_MODIFIED_BY, request.getModifiedBy());
		params.addValue(PARAM_REQ_ID, request.getReqId());

		int updatedCount = jdbcTemplate.update(query, params);

		if (updatedCount == 0) {
			log.error("Zero requests have been updated");
			throw new InternalErrorException("Zero requests have been updated");
		} else if (updatedCount > 1) {
			log.error("Only one request should have been updated");
			throw new InternalErrorException("Only one request should have been updated");
		}

		return true;
	}

	@Override
	@Transactional
	public boolean deleteRequest(@NonNull Long reqId) throws InternalErrorException {
		String query = new StringJoiner(" ")
				.add("DELETE FROM").add(REQUESTS_TABLE)
				.add("WHERE id = :req_id")
				.toString();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue(PARAM_REQ_ID, reqId);

		int updatedCount = jdbcTemplate.update(query, params);

		if (updatedCount == 0) {
			log.error("Zero requests have been deleted");
			throw new InternalErrorException("Zero requests have been deleted");
		} else if (updatedCount > 1) {
			log.error("Only one request should have been deleted");
			throw new InternalErrorException("Only one request should have been deleted");
		}

		return true;
	}

	@Override
	@Transactional
	public RequestDTO getRequestById(@NonNull Long reqId) {
		String query = new StringJoiner(" ")
				.add("SELECT * FROM").add(REQUESTS_TABLE)
				.add("WHERE id = :req_id")
				.toString();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue(PARAM_REQ_ID, reqId);

		try {
			return jdbcTemplate.queryForObject(query, params, REQUEST_MAPPER);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	@Transactional
	public List<RequestDTO> getAllRequests() {
		String query = new StringJoiner(" ")
				.add("SELECT * FROM").add(REQUESTS_TABLE)
				.toString();

		return jdbcTemplate.query(query, REQUEST_MAPPER);
	}

	@Override
	@Transactional
	public List<RequestDTO> getAllRequestsByUserId(@NonNull Long userId) {
		String query = new StringJoiner(" ")
				.add("SELECT * FROM").add(REQUESTS_TABLE)
				.add("WHERE requesting_user_id = :req_user_id")
				.toString();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue(PARAM_REQ_USER_ID, userId);

		return jdbcTemplate.query(query, params, REQUEST_MAPPER);
	}

	@Override
	@Transactional
	public List<RequestDTO> getAllRequestsByStatus(@NonNull RequestStatus status) {
		String query = new StringJoiner(" ")
				.add("SELECT * FROM").add(REQUESTS_TABLE)
				.add("WHERE status = :status")
				.toString();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue(PARAM_STATUS, status.getAsInt());

		return jdbcTemplate.query(query, params, REQUEST_MAPPER);
	}

	@Override
	@Transactional
	public List<RequestDTO> getAllRequestsByAction(@NonNull RequestAction action) {
		String query = new StringJoiner(" ")
				.add("SELECT * FROM").add(REQUESTS_TABLE)
				.add("WHERE action = :action")
				.toString();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue(PARAM_ACTION, action.getAsInt());
		return jdbcTemplate.query(query, params, REQUEST_MAPPER);
	}

	@Override
	@Transactional
	public List<RequestDTO> getAllRequestsByFacilityId(@NonNull Long facilityId) {
		String query = new StringJoiner(" ")
				.add("SELECT * FROM").add(REQUESTS_TABLE)
				.add("WHERE facility_id = :fac_id")
				.toString();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue(PARAM_FAC_ID, facilityId);

		return jdbcTemplate.query(query, params, REQUEST_MAPPER);
	}

	@Override
	@Transactional
	public List<RequestDTO> getAllRequestsByFacilityIds(@NonNull Set<Long> facilityIds) {
		String query = new StringJoiner(" ")
				.add("SELECT * FROM").add(REQUESTS_TABLE)
				.add("WHERE facility_id IN (:ids)")
				.toString();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue(PARAM_IDS, new ArrayList<>(facilityIds));

		return jdbcTemplate.query(query, params, REQUEST_MAPPER);
	}

	@Override
	@Transactional
	public Long getActiveRequestIdByFacilityId(@NonNull Long facilityId) throws InternalErrorException {
		String query = new StringJoiner(" ")
				.add("SELECT id FROM").add(REQUESTS_TABLE)
				.add("WHERE facility_id = :fac_id AND (status = :status_wfc OR status = :status_wfa)")
				.toString();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue(PARAM_FAC_ID, facilityId);
		params.addValue(PARAM_STATUS_WFA, RequestStatus.WAITING_FOR_CHANGES.getAsInt());
		params.addValue(PARAM_STATUS_WFC, RequestStatus.WAITING_FOR_APPROVAL.getAsInt());

		Long activeRequestId;
		try {
			activeRequestId = jdbcTemplate.queryForObject(query, params, Long.class);
		} catch (EmptyResultDataAccessException e) {
			activeRequestId = null;
		} catch (IncorrectResultSizeDataAccessException e) {
			log.error("Two active requests for facility {} found", facilityId);
			throw new InternalErrorException("Two active requests for facility #" + facilityId + " found", e);
		}

		return activeRequestId;
	}

}
