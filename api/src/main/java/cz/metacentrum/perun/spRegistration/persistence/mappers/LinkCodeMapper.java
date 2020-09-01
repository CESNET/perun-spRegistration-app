package cz.metacentrum.perun.spRegistration.persistence.mappers;

import cz.metacentrum.perun.spRegistration.common.models.LinkCode;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Mapper for LinkCode.ts object. Maps result retrieved from DB to LinkCode.ts object.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@Slf4j
public class LinkCodeMapper implements RowMapper<LinkCode> {

	private static final String HASH_KEY = "hash";
	private static final String RECIPIENT_EMAIL_KEY = "recipient_email";
	private static final String SENDER_NAME_KEY = "sender_name";
	private static final String SENDER_EMAIL_KEY = "sender_email";
	private static final String EXPIRES_AT_KEY = "expires_at";
	private static final String FACILITY_ID_KEY = "facility_id";
	private static final String REQUEST_ID_KEY = "request_id";

	@Override
	public LinkCode mapRow(@NotNull ResultSet resultSet, int i) throws SQLException {
		LinkCode code = new LinkCode();

		code.setHash(resultSet.getString(HASH_KEY));
		code.setRecipientEmail(resultSet.getString(RECIPIENT_EMAIL_KEY));
		code.setSenderName(resultSet.getString(SENDER_NAME_KEY));
		code.setSenderEmail(resultSet.getString(SENDER_EMAIL_KEY));
		code.setExpiresAt(new Timestamp(resultSet.getLong(EXPIRES_AT_KEY)));
		code.setFacilityId(resultSet.getLong(FACILITY_ID_KEY));
		code.setRequestId(resultSet.getLong(REQUEST_ID_KEY));

		return code;
	}

}
