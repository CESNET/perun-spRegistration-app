package cz.metacentrum.perun.spRegistration.persistence.managers.impl;

import cz.metacentrum.perun.spRegistration.persistence.configs.Config;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestAction;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestStatus;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.CreateRequestException;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestManager;
import cz.metacentrum.perun.spRegistration.persistence.mappers.RequestMapper;
import cz.metacentrum.perun.spRegistration.persistence.mappers.RequestSignatureMapper;
import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import cz.metacentrum.perun.spRegistration.persistence.models.RequestSignature;
import cz.metacentrum.perun.spRegistration.service.exceptions.InternalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Implementation of Request Manager. Works with DB and Request objects.
 *
 * @author Dominik Frantisek Bucik &lt;bucik@ics.muni.cz&gt;
 */
@EnableTransactionManagement
public class RequestManagerImpl implements RequestManager {

	private static final Logger log = LoggerFactory.getLogger(RequestManagerImpl.class);
	private static final String REQUESTS_TABLE = "requests";
	private static final String APPROVALS_TABLE = "approvals";
	private static final String CODES_TABLE = "signatureCodes";

	private final RequestMapper REQUEST_MAPPER;
	private final RequestSignatureMapper REQUEST_SIGNATURE_MAPPER;
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Autowired
	public RequestManagerImpl(Config config) {
		REQUEST_SIGNATURE_MAPPER = new RequestSignatureMapper();
		REQUEST_MAPPER = new RequestMapper(config);
	}

	@Override
	public void setJdbcTemplate(JdbcTemplate template) {
		if (template == null) {
			log.error("Illegal parameters passed: template IS NULL");
			throw new IllegalArgumentException();
		} else if (template.getDataSource() == null) {
			log.error("Illegal parameters passed: template.dataSource IS NULL");
			throw new IllegalArgumentException();
		}
		
		this.jdbcTemplate = new NamedParameterJdbcTemplate(template.getDataSource());
	}

	@Override
	@Transactional
	public Long createRequest(Request request) throws InternalErrorException, CreateRequestException {
		log.trace("createRequest({})", request);
		if (request == null) {
			log.error("Illegal parameters passed: request IS NULL");
			throw new IllegalArgumentException();
		}

		Long activeRequestId = this.getActiveRequestIdByFacilityId(request.getFacilityId());
		if (activeRequestId != null) {
			log.error("Active requests already exist for facilityId: {}", request.getFacilityId());
			throw new CreateRequestException();
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
		params.addValue("attributes", request.getAttributesAsJsonForDb());
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
		if (request == null) {
			log.error("Illegal parameters passed: request IS NULL");
			throw new IllegalArgumentException();
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
		params.addValue("attributes", request.getAttributesAsJsonForDb());
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
		if (reqId == null) {
			log.error("Illegal parameters passed: reqId IS NULL");
			throw new IllegalArgumentException();
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
		if (reqId == null) {
			log.error("Illegal parameters passed: reqId IS NULL");
			throw new IllegalArgumentException();
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
		if (userId == null) {
			log.error("Illegal parameters passed: userId IS NULL");
			throw new IllegalArgumentException();
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
		if (status == null) {
			log.error("Illegal parameters passed: status IS NULL");
			throw new IllegalArgumentException();
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
		if (action == null) {
			log.error("Illegal parameters passed: action IS NULL");
			throw new IllegalArgumentException();
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
		if (facilityId == null) {
			log.error("Illegal parameters passed: facilityId IS NULL");
			throw new IllegalArgumentException();
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
		if (facilityIds == null || facilityIds.isEmpty()) {
			log.error("Illegal parameters passed: facilityIds IS NULL OR EMPTY: {}", facilityIds);
			throw new IllegalArgumentException();
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
		if (facilityId == null) {
			return null;
		}

		List<Integer> allowedStatuses = Arrays.asList(RequestStatus.APPROVED.getAsInt(), RequestStatus.REJECTED.getAsInt());

		String query = new StringJoiner(" ")
				.add("SELECT id FROM").add(REQUESTS_TABLE)
				.add("WHERE facility_id = :fac_id AND status NOT IN (:allowed_statuses)")
				.toString();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("fac_id", facilityId);
		params.addValue("allowed_statuses", allowedStatuses);

		Long activeRequestId;
		try {
			activeRequestId = jdbcTemplate.queryForObject(query, params, Long.class);
		} catch (EmptyResultDataAccessException e) {
			activeRequestId = null;
		} catch (IncorrectResultSizeDataAccessException e) {
			log.error("Two active requests for one facility found");
			throw new InternalErrorException("Two active requests for one facility found", e);
		}

		log.trace("getActiveRequestIdByFacilityId returns: {}", activeRequestId);
		return activeRequestId;
	}

	@Override
	@Transactional
	public boolean addSignature(Long requestId, Long userId, String userName, boolean approved, String code)
			throws InternalErrorException {
		log.trace("addSignature(requestId: {}, userId: {}, userName: {}, approved: {})",
				requestId, userId, userName, approved);
		if (requestId == null || userId == null || userName == null || userName.isEmpty()) {
			log.error("Wrong parameters passed: (requestId: {}, user:Id {}, userName: {})", requestId, userId, userName);
			throw new IllegalArgumentException();
		}
		
		String query = new StringJoiner(" ")
				.add("INSERT INTO").add(APPROVALS_TABLE)
				.add("(request_id, user_id, name, approved) VALUES (:request_id, :user_id, :username, :approved)")
				.toString();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("request_id", requestId);
		params.addValue("user_id", userId);
		params.addValue("username", userName);
		params.addValue("approved", approved);

		int updatedCount = jdbcTemplate.update(query, params);

		if (updatedCount == 0) {
			log.error("Zero approvals have been inserted");
			throw new InternalErrorException("Zero approvals have been inserted");
		} else if (updatedCount > 1) {
			log.error("Only one approval should have been inserted");
			throw new InternalErrorException("Only one approval should have been inserted");
		}

		query = new StringJoiner(" ")
				.add("DELETE FROM").add(CODES_TABLE)
				.add("WHERE code = :code")
				.toString();
		params = new MapSqlParameterSource();
		params.addValue("code", code);

		updatedCount = jdbcTemplate.update(query, params);

		if (updatedCount == 0) {
			log.error("Zero codes deleted");
			throw new InternalErrorException("Zero codes deleted");
		} else if (updatedCount > 1) {
			log.error("Only one code should be deleted");
			throw new InternalErrorException("Only one code should have been inserted");
		}
		
		log.trace("addSignature returns: true");
		return true;
	}

	@Override
	@Transactional
	public List<RequestSignature> getRequestSignatures(Long requestId) {
		log.trace("getRequestSignatures({})", requestId);
		if (requestId == null) {
			log.error("Illegal parameters passed: requestId IS NULL");
			throw new IllegalArgumentException();
		}

		String query = new StringJoiner(" ")
				.add("SELECT * FROM").add(APPROVALS_TABLE)
				.add("WHERE request_id = :request_id")
				.toString();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("request_id", requestId);

		List<RequestSignature> foundApprovals = jdbcTemplate.query(query, params, REQUEST_SIGNATURE_MAPPER);

		log.trace("getRequestSignatures returns: {}", foundApprovals);
		return foundApprovals;
	}

	@Override
	public boolean validateCode(String code) {
		log.trace("validateCode({})", code);

		if (code == null || code.isEmpty()) {
			log.error("Illegal argument - code is null or empty: {}", code);
			throw new IllegalArgumentException("Illegal argument - code is null or empty: " + code);
		}

		String query = new StringJoiner(" ")
				.add("SELECT count(code) FROM").add(CODES_TABLE)
				.add("WHERE code = :code")
				.toString();
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("code", code);

		Integer foundCodes = jdbcTemplate.queryForObject(query, params, Integer.class);
		boolean isValid = ((foundCodes != null) && (foundCodes == 1));

		log.trace("validateCode() returns: {}", isValid);
		return isValid;
	}

	@Override
	public int storeCodes(List<String> codes) throws InternalErrorException {
		log.trace("storeCodes({})", codes);

		if (codes == null || codes.isEmpty()) {
			log.error("Illegal argument - codes is null or empty: {}", codes);
			throw new IllegalArgumentException("Illegal argument - codes is null or empty: " + codes);
		}

		String query = new StringJoiner(" ")
				.add("INSERT INTO").add(CODES_TABLE).add("(code)")
				.add("VALUES (:code)")
				.toString();

		List<MapSqlParameterSource> batchArgs = new ArrayList<>();
		for (String code: codes) {
			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("code", code);
			batchArgs.add(parameters);
		}

		int[] insertedCodes = jdbcTemplate.batchUpdate(query, batchArgs.toArray(new MapSqlParameterSource[codes.size()]));
		int sum = 0;
		for (int i : insertedCodes) {
			if (i != 1) {
				log.error("Inserting code failed");
				throw new InternalErrorException("Inserting code failed");
			} else {
				sum++;
			}
		}

		if (sum != codes.size()) {
			log.error("Expected {} inserts, made {}", codes.size(), sum);
			throw new InternalErrorException("Expected " + codes.size() + " inserts, made " + sum);
		}

		log.trace("storeCodes() returns: {}", sum);
		return sum;
	}
}
