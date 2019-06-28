package cz.metacentrum.perun.spRegistration.persistence.mappers;

import cz.metacentrum.perun.spRegistration.persistence.models.RequestSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Mapper for RequestSignature object. Maps result retrieved from DB to RequestSignature object.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class RequestSignatureMapper implements RowMapper<RequestSignature> {

	private static final Logger log = LoggerFactory.getLogger(RequestSignatureMapper.class);

	private static final String REQUEST_ID_KEY = "request_id";
	private static final String USER_ID_KEY = "user_id";
	private static final String SIGNED_AT_KEY = "signed_at";
	private static final String NAME_KEY = "name";
	private static final String APPROVED_KEY = "approved";

	@Override
	public RequestSignature mapRow(ResultSet resultSet, int i) throws SQLException {
		log.trace("mapRow(resultSet: {}, i: {})", resultSet, i);
		RequestSignature approval = new RequestSignature();

		approval.setRequestId(resultSet.getLong(REQUEST_ID_KEY));
		approval.setUserId(resultSet.getLong(USER_ID_KEY));
		approval.setSignedAt(resultSet.getTimestamp(SIGNED_AT_KEY).toLocalDateTime());
		approval.setName(resultSet.getString(NAME_KEY));
		approval.setApproved(resultSet.getBoolean(APPROVED_KEY));

		log.trace("mapRow() returns: {}", approval);
		return approval;
	}
}