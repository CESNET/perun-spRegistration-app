package cz.metacentrum.perun.spRegistration.persistence.mappers;

import cz.metacentrum.perun.spRegistration.persistence.models.RequestSignature;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RequestSignatureMapper implements RowMapper<RequestSignature> {

	private static final String FACILITY_ID_KEY = "facility_id";
	private static final String LINK_KEY = "link";
	private static final String HASH_KEY = "hash";
	private static final String SIGNER_EMAIL_KEY = "user_email";
	private static final String SIGNER_ID_KEY = "user_id";
	private static final String SIGNER_NAME_KEY = "user_name";
	private static final String SIGNED_AT_KEY = "signed_at";
	private static final String VALID_UNTIL_KEY = "valid_until";

	@Override
	public RequestSignature mapRow(ResultSet resultSet, int i) throws SQLException {
		RequestSignature approval = new RequestSignature();
		approval.setFacilityId(resultSet.getLong(FACILITY_ID_KEY));
		approval.setLink(resultSet.getString(LINK_KEY));
		approval.setHash(resultSet.getString(HASH_KEY));
		approval.setSignerId(resultSet.getLong(SIGNER_ID_KEY));
		approval.setSignerName(resultSet.getString(SIGNER_NAME_KEY));
		approval.setSignerEmail(resultSet.getString(SIGNER_EMAIL_KEY));
		if (resultSet.getTimestamp(SIGNED_AT_KEY) != null) {
			approval.setSignedAt(resultSet.getTimestamp(SIGNED_AT_KEY).toLocalDateTime());
		}

		if (resultSet.getTimestamp(VALID_UNTIL_KEY) != null) {
			approval.setValidUntil(resultSet.getTimestamp(VALID_UNTIL_KEY).toLocalDateTime());
		}

		return approval;
	}
}