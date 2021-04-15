package cz.metacentrum.perun.spRegistration.persistence.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.metacentrum.perun.spRegistration.common.configs.AppBeansContainer;
import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.enums.RequestAction;
import cz.metacentrum.perun.spRegistration.common.enums.RequestStatus;
import cz.metacentrum.perun.spRegistration.common.models.AttrInput;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.common.models.RequestDTO;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class RequestMapper implements RowMapper<RequestDTO> {

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

	public RequestMapper(@NonNull AppBeansContainer appBeansContainer) {
		this.definitionMap = appBeansContainer.getAttributeDefinitionMap();
		this.attrInputMap = appBeansContainer.getAttrInputMap();
	}

	@Override
	public RequestDTO mapRow(ResultSet resultSet, int i) throws SQLException {
		String attrsJsonStr = resultSet.getString(ATTRIBUTES_KEY);
		Map<AttributeCategory, Map<String, PerunAttribute>> attrs = null;
		try {
			attrs = mapAttributes(attrsJsonStr);
		} catch (JsonProcessingException e) {
			//TODO: handle
		}
		Long facilityId = resultSet.getLong(FACILITY_ID_KEY);
		if (resultSet.wasNull()) {
			facilityId = null;
		}

		RequestDTO request = new RequestDTO();
		request.setReqId(resultSet.getLong(ID_KEY));
		request.setFacilityId(facilityId);
		request.setStatus(RequestStatus.resolve(resultSet.getInt(STATUS_KEY)));
		request.setAction(RequestAction.resolve(resultSet.getInt(ACTION_KEY)));
		request.setReqUserId(resultSet.getLong(REQUESTING_USER_ID_KEY));
		request.setModifiedAt(resultSet.getTimestamp(MODIFIED_AT_KEY));
		request.setModifiedBy(resultSet.getLong(MODIFIED_BY_KEY));
		request.setAttributes(attrs);
		return request;
	}

	private Map<AttributeCategory, Map<String, PerunAttribute>> mapAttributes(@NonNull String attrsJsonStr)
			throws JsonProcessingException
	{
		Map<AttributeCategory, Map<String, PerunAttribute>> attributes = new HashMap<>();
		ObjectNode json = (ObjectNode) new ObjectMapper().readTree(attrsJsonStr);
		Iterator<String> categoryKeys = json.fieldNames();

		while (categoryKeys.hasNext()) {
			String key = categoryKeys.next();
			AttributeCategory category = AttributeCategory.fromString(key);
			ObjectNode categoryJson = (ObjectNode) json.get(key);
			Map<String, PerunAttribute> mappedAttributesForCategory = new HashMap<>();
			Iterator<String> attributeKeys = categoryJson.fieldNames();
			while (attributeKeys.hasNext()) {
				String attrName = attributeKeys.next();
				PerunAttribute mappedAttribute = PerunAttribute.fromJsonOfDb(attrName, categoryJson.get(attrName),
						definitionMap, attrInputMap);
				if (mappedAttribute != null) {
					mappedAttributesForCategory.put(attrName, mappedAttribute);
				}
			}

			attributes.put(category, mappedAttributesForCategory);
		}

		return attributes;
	}

}
