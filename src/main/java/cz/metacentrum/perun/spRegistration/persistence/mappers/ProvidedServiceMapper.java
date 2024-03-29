package cz.metacentrum.perun.spRegistration.persistence.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.metacentrum.perun.spRegistration.common.models.ProvidedService;
import cz.metacentrum.perun.spRegistration.persistence.enums.ServiceEnvironment;
import cz.metacentrum.perun.spRegistration.persistence.enums.ServiceProtocol;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

/**
 * Mapper for RequestSignature object. Maps result retrieved from DB to RequestSignature object.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class ProvidedServiceMapper implements RowMapper<ProvidedService> {

	private static final Logger log = LoggerFactory.getLogger(ProvidedServiceMapper.class);

	public static final String ID_KEY = "id";
	public static final String FACILITY_ID_KEY = "facility_id";
	public static final String NAME_KEY = "name";
	public static final String DESCRIPTION_KEY = "description";
	public static final String PROTOCOL_KEY = "protocol";
	public static final String ENVIRONMENT_KEY = "environment";
	public static final String IDENTIFIER_KEY = "identifier";
	public static final String FAC_DELETED_KEY = "facility_deleted";

	@Override
	public ProvidedService mapRow(ResultSet resultSet, int i) throws SQLException {
		ProvidedService sp = new ProvidedService();

		sp.setId(resultSet.getLong(ID_KEY));
		sp.setFacilityId(resultSet.getLong(FACILITY_ID_KEY));
		try {
			sp.nameFromDbJson(resultSet.getString(NAME_KEY));
			sp.descriptionFromDbJson(resultSet.getString(DESCRIPTION_KEY));
		} catch (JsonProcessingException e) {
			log.error("Could not read name or description from DB: {}", sp);
			return null;
		}
		sp.setProtocol(ServiceProtocol.valueOf(resultSet.getString(PROTOCOL_KEY)));
		sp.setEnvironment(ServiceEnvironment.valueOf(resultSet.getString(ENVIRONMENT_KEY)));
		sp.setIdentifier(resultSet.getString(IDENTIFIER_KEY));
		sp.setFacilityDeleted(resultSet.getBoolean(FAC_DELETED_KEY));
		return sp;
	}

}