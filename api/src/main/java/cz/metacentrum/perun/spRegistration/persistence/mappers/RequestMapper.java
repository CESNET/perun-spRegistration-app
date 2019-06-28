package cz.metacentrum.perun.spRegistration.persistence.mappers;

import cz.metacentrum.perun.spRegistration.Utils;
import cz.metacentrum.perun.spRegistration.persistence.configs.Config;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestAction;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestStatus;
import cz.metacentrum.perun.spRegistration.persistence.models.AttrInput;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Mapper for Request object. Maps result retrieved from DB to Request object.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class RequestMapper implements RowMapper<Request> {

	private static final Logger log = LoggerFactory.getLogger(RequestMapper.class);

	private static final String ATTRIBUTES_KEY = "attributes";
	private static final String ID_KEY = "id";
	private static final String FACILITY_ID_KEY = "facility_id";
	private static final String STATUS_KEY = "status";
	private static final String ACTION_KEY = "action";
	private static final String REQUESTING_USER_ID_KEY = "requesting_user_id";
	private static final String MODIFIED_BY_KEY = "modified_by";
	private static final String MODIFIED_AT_KEY = "modified_at";

	private final Map<String, PerunAttributeDefinition> definitionMap;
	private final Map<String, AttrInput> attrInputMap;

	public RequestMapper(Config config) {
		if (config.getAppConfig() != null) {
			this.definitionMap = config.getAppConfig().getPerunAttributeDefinitionsMap();
			this.attrInputMap = config.getInputMap();
		} else {
			this.definitionMap = null;
			this.attrInputMap = null;
		}
	}

	@Override
	public Request mapRow(ResultSet resultSet, int i) throws SQLException {
		log.trace("mapRow(resultSet: {}, i: {})", resultSet, i);

		String attrsJsonStr = resultSet.getString(ATTRIBUTES_KEY);
		Map<String, PerunAttribute> attrs = mapAttributes(attrsJsonStr);
		Long facilityId = resultSet.getLong(FACILITY_ID_KEY);
		if (resultSet.wasNull()) {
			facilityId = null;
		}

		Request request = new Request();
		request.setReqId(resultSet.getLong(ID_KEY));
		request.setFacilityId(facilityId);
		request.setStatus(RequestStatus.resolve(resultSet.getInt(STATUS_KEY)));
		request.setAction(RequestAction.resolve(resultSet.getInt(ACTION_KEY)));
		request.setReqUserId(resultSet.getLong(REQUESTING_USER_ID_KEY));
		request.setModifiedAt(resultSet.getTimestamp(MODIFIED_AT_KEY));
		request.setModifiedBy(resultSet.getLong(MODIFIED_BY_KEY));
		request.setAttributes(attrs);

		log.trace("mapRow() returns: {}", request);
		return request;
	}

	private Map<String, PerunAttribute> mapAttributes(String attrsJsonStr) {
		log.trace("mapAttributes({})", attrsJsonStr);

		Map<String, PerunAttribute> attributes = new HashMap<>();

		if (!Utils.checkParamsInvalid(attrsJsonStr)) {
			JSONObject json = new JSONObject(attrsJsonStr);
			Iterator<String> keys = json.keys();

			while (keys.hasNext()) {
				String key = keys.next();
				PerunAttribute mappedAttribute = PerunAttribute.fromJsonOfDb(
						key, json.getJSONObject(key), definitionMap, attrInputMap
				);
				attributes.put(key, mappedAttribute);
			}
		}

		log.trace("mapAttributes() returns: {}", attributes);
		return attributes;
	}
}
