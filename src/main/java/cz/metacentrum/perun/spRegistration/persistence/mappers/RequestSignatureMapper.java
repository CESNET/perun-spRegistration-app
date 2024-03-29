package cz.metacentrum.perun.spRegistration.persistence.mappers;

import cz.metacentrum.perun.spRegistration.common.models.RequestSignatureDTO;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

/**
 * Mapper for RequestSignature object. Maps result retrieved from DB to RequestSignature object.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@Slf4j
public class RequestSignatureMapper implements RowMapper<RequestSignatureDTO> {

	private static final String REQUEST_ID_KEY = "request_id";
	private static final String USER_ID_KEY = "user_id";
	private static final String SIGNED_AT_KEY = "signed_at";
	private static final String NAME_KEY = "name";
	private static final String APPROVED_KEY = "approved";

	@Override
	public RequestSignatureDTO mapRow(ResultSet resultSet, int i) throws SQLException {
		RequestSignatureDTO approval = new RequestSignatureDTO();

		approval.setRequestId(resultSet.getLong(REQUEST_ID_KEY));
		approval.setUserId(resultSet.getLong(USER_ID_KEY));
		approval.setSignedAt(resultSet.getTimestamp(SIGNED_AT_KEY).toLocalDateTime());
		approval.setName(resultSet.getString(NAME_KEY));
		approval.setApproved(resultSet.getBoolean(APPROVED_KEY));

		return approval;
	}

}