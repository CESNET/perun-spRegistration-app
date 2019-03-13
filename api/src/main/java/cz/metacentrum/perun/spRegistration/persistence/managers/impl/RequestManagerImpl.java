package cz.metacentrum.perun.spRegistration.persistence.managers.impl;

import cz.metacentrum.perun.spRegistration.persistence.configs.AppConfig;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestAction;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestStatus;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestManager;
import cz.metacentrum.perun.spRegistration.persistence.mappers.RequestSignatureMapper;
import cz.metacentrum.perun.spRegistration.persistence.mappers.RequestMapper;
import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import cz.metacentrum.perun.spRegistration.persistence.models.RequestSignature;
import cz.metacentrum.perun.spRegistration.persistence.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.datetime.joda.LocalDateTimeParser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("Duplicates")
public class RequestManagerImpl implements RequestManager {

	private static final Logger log = LoggerFactory.getLogger(RequestManagerImpl.class);
	private static final String REQUESTS_TABLE = " requests ";
	private static final String APPROVALS_TABLE = " approvals ";

	private RequestMapper requestMapper;
	private final RequestSignatureMapper REQUEST_SIGNATURE_MAPPER;
	private NamedParameterJdbcTemplate jdbcTemplate;

	public RequestManagerImpl() {
		REQUEST_SIGNATURE_MAPPER = new RequestSignatureMapper();
	}

	public void setAppConfig(AppConfig config) {
		requestMapper = new RequestMapper(config);
	}

	@Override
	public void setJdbcTemplate(JdbcTemplate template) {
		if (template == null) {
			log.error("Trying to provide NULL as jdbcTemplate");
			throw new IllegalArgumentException("JDBCTemplate is null");
		} else if (template.getDataSource() == null) {
			log.error("Trying to provide jdbcTemplate without assigned dataSource");
			throw new IllegalArgumentException("JDBCTemplate does not contain dataSource");
		}
		this.jdbcTemplate = new NamedParameterJdbcTemplate(template.getDataSource());
	}

	@Override
	public Long createRequest(Request request) {
		log.debug("createRequest({})", request);
		String query = "INSERT INTO" + REQUESTS_TABLE +
				"(facility_id, status, action, requesting_user_id, attributes, modified_by) " +
				"VALUES (:fac_id, :status, :action, :req_user_id, :attributes, :modified_by)";

		KeyHolder key = new GeneratedKeyHolder();
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("fac_id", request.getFacilityId());
		params.addValue("status", request.getStatus().getAsInt());
		params.addValue("action", request.getAction().getAsInt());
		params.addValue("req_user_id", request.getReqUserId());
		params.addValue("attributes", request.getAttributesAsJsonForDb());
		params.addValue("modified_by", request.getModifiedBy());

		jdbcTemplate.update(query, params, key, new String[] { "id" });
		Long result = (Long) key.getKey();

		log.debug("createRequest returns: {}", result);
		return result;
	}

	@Override
	public boolean updateRequest(Request request) {
		log.debug("updateRequest({})", request);
		String query = "UPDATE" + REQUESTS_TABLE +
				"SET facility_id = :fac_id, status = :status, action = :action, requesting_user_id = :req_user_id, " +
				"attributes = :attributes, modified_by = :modified_by, modified_at = NOW()" +
				"WHERE id = :req_id";

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("fac_id", request.getFacilityId());
		params.addValue("status", request.getStatus().getAsInt());
		params.addValue("action", request.getAction().getAsInt());
		params.addValue("req_user_id", request.getReqUserId());
		params.addValue("attributes", request.getAttributesAsJsonForDb());
		params.addValue("modified_by", request.getModifiedBy());
		params.addValue("req_id", request.getReqId());

		jdbcTemplate.update(query, params);

		log.debug("updateRequest returns: {}", true);
		return true;
	}

	@Override
	public boolean deleteRequest(Long reqId) {
		log.debug("deleteRequest({})", reqId);
		String query = "DELETE FROM" + REQUESTS_TABLE + "WHERE id = :req_id";

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("req_id", reqId);

		jdbcTemplate.update(query, params);

		log.debug("deleteRequest returns: {}", true);
		return true;
	}

	@Override
	public Request getRequestByReqId(Long reqId) {
		log.debug("getRequestByReqId({})", reqId);
		String query = "SELECT * FROM" + REQUESTS_TABLE + "WHERE id = :req_id";

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("req_id", reqId);

		Request request = jdbcTemplate.queryForObject(query, params, requestMapper);

		log.debug("getRequestByReqId returns: {}", request);
		return request;
	}

	@Override
	public List<Request> getAllRequests() {
		log.debug("getAllRequests()");
		String query = "SELECT * FROM" + REQUESTS_TABLE;

		List<Request> requests = jdbcTemplate.query(query, requestMapper);

		log.debug("getAllRequests returns: {}", requests);
		return requests;
	}

	@Override
	public List<Request> getAllRequestsByUserId(Long userId) {
		log.debug("getAllRequestsByUserId({})", userId);
		String query = "SELECT * FROM" + REQUESTS_TABLE + "WHERE requesting_user_id = :req_user_id";

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("req_user_id", userId);

		List<Request> requests = jdbcTemplate.query(query, params, requestMapper);

		log.debug("getAllRequestsByUserId returns: {}", requests);
		return requests;
	}

	@Override
	public List<Request> getAllRequestsByStatus(RequestStatus status) {
		log.debug("getAllRequestsByStatus({})", status);
		String query = "SELECT * FROM" + REQUESTS_TABLE + "WHERE status = :status";

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("status", status.getAsInt());

		List<Request> requests = jdbcTemplate.query(query, params, requestMapper);

		log.debug("getAllRequestsByStatus returns: {}", requests);
		return requests;
	}

	@Override
	public List<Request> getAllRequestsByAction(RequestAction action) {
		log.debug("getAllRequestsByAction({})", action);
		String query = "SELECT * FROM" + REQUESTS_TABLE + "WHERE action = :action";

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("action", action.getAsInt());

		List<Request> requests = jdbcTemplate.query(query, params, requestMapper);

		log.debug("getAllRequestsByAction returns: {}", requests);
		return requests;
	}

	@Override
	public List<Request> getAllRequestsByFacilityId(Long facilityId) {
		log.debug("getAllRequestsByFacilityId({})", facilityId);
		String query = "SELECT * FROM" + REQUESTS_TABLE + "WHERE facility_id = :fac_id";

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("fac_id", facilityId);

		List<Request> requests = jdbcTemplate.query(query, params, requestMapper);

		log.debug("getAllRequestsByFacilityId returns: {}", requests);
		return requests;
	}

	@Override
	public List<Request> getAllRequestsByFacilityIds(Set<Long> facilityIds) {
		log.debug("getAllRequestsByFacilityIds({})", facilityIds);
		if (facilityIds == null || facilityIds.isEmpty()) {
			return new ArrayList<>();
		}
		String query = "SELECT * FROM" + REQUESTS_TABLE + "WHERE facility_id IN (:ids)";

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("ids", new ArrayList<>(facilityIds));

		List<Request> requests = jdbcTemplate.query(query, params, requestMapper);

		log.debug("getAllRequestsByFacilityIds returns: {}", requests);
		return requests;
	}

	@Override
	public boolean addSignature(Long facilityId, String hash, User user, LocalDateTime signedAt) {
		log.debug("addSignature(facilityId: {}, hash: {}, user: {}, signedAt: {})", facilityId, hash, user, signedAt);
		String query = "UPDATE" + APPROVALS_TABLE +
				"SET user_id = :user_id, user_name = :user_name, signed_at = :signed_at " +
				"WHERE facility_id = :fac_id AND hash = :hash";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("user_id", user.getId());
		params.addValue("user_name", user.getFullName());
		params.addValue("user_email", user.getEmail());
		params.addValue("signed_at", Timestamp.valueOf(signedAt));
		params.addValue("fac_id", facilityId);
		params.addValue("hash", hash);

		int res = jdbcTemplate.update(query, params);

		if (res == 1) {
			log.debug("addSignature returns: {}", true);
			return true;
		}

		//todo Exception?
		return false;
	}

	@Override
	public List<RequestSignature> getRequestSignatures(Long facilityId) {
		log.debug("getRequestSignatures({})", facilityId);
		String query = "SELECT * FROM" + APPROVALS_TABLE + "WHERE facility_id = :fac_id";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("fac_id", facilityId);

		List<RequestSignature> approvals = jdbcTemplate.query(query, params, REQUEST_SIGNATURE_MAPPER);

		log.debug("getRequestSignatures returns: {}", approvals);
		return approvals;
	}

	@Override
	public boolean storeApprovalLink(String authority, String hash, Long facilityId, String link, LocalDateTime validUntil) {
		log.debug("storeApprovalLink(authority: {}, hash {}, facilityId: {}, link: {}, validUntil: {})",
				authority, hash, facilityId, link, validUntil);
		String query = "INSERT INTO" + APPROVALS_TABLE +
				"(facility_id, link, hash, user_email, valid_until) " +
				"VALUES (:fac_id, :link, :hash, :user_email, :valid_until)";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("fac_id", facilityId);
		params.addValue("link", link);
		params.addValue("hash", hash);
		params.addValue("user_email", authority);
		params.addValue("valid_until", Timestamp.valueOf(validUntil));

		jdbcTemplate.update(query, params);

		log.debug("storeApprovalLink returns: {}", true);
		return true;
	}

}
