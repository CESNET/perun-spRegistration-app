package cz.metacentrum.perun.spRegistration.common.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.metacentrum.perun.spRegistration.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Representation of attribute from Perun.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class PerunAttribute {

	private final static String STRING_TYPE = "java.lang.String";
	private final static String INTEGER_TYPE = "java.lang.Integer";
	private final static String BOOLEAN_TYPE = "java.lang.Boolean";
	private final static String ARRAY_TYPE = "java.util.ArrayList";
	private final static String MAP_TYPE = "java.util.LinkedHashMap";
	private final static String LARGE_STRING_TYPE = "java.lang.LargeString";
	private final static String LARGE_ARRAY_LIST_TYPE = "java.util.LargeArrayList";

	private PerunAttributeDefinition definition;
	private Object value;
	private Object oldValue;
	private String comment;
	private String fullName;
	private AttrInput input;

	public PerunAttribute() { }

	public PerunAttribute(PerunAttributeDefinition definition, String fullName, Object value, Object oldValue, String comment,
						  AttrInput input) {
		this.definition = definition;
		this.fullName = fullName;
		this.value = value;
		if (definition != null && BOOLEAN_TYPE.equals(definition.getType())) {
			if (value == null) {
				this.value = false;
			}
		}
		this.oldValue = oldValue;
		if (definition != null && BOOLEAN_TYPE.equals(definition.getType())) {
			if (oldValue == null) {
				this.oldValue = false;
			}
		}
		this.comment = comment;
		this.input = input;
	}

	public PerunAttribute(PerunAttributeDefinition attributeDefinition, Object value) {
		this.definition = attributeDefinition;
		this.fullName = attributeDefinition.getFullName();
		this.value = value;
		if (BOOLEAN_TYPE.equals(definition.getType())) {
			if (value == null) {
				this.value = false;
			}
		}
		this.oldValue = null;
		this.comment = null;
		this.input = null;
	}

	public PerunAttributeDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(PerunAttributeDefinition definition) {
		this.definition = definition;
	}

	public String getFullName() {
		return (fullName == null) ? definition.getFullName() : fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public void setOldValue(Object oldValue) {
		this.oldValue = oldValue;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public AttrInput getInput() {
		return input;
	}

	public void setInput(AttrInput input) {
		this.input = input;
	}

	public String valueAsString() {
		return valueAsString(false);
	}

	public Long valueAsLong() {
		return valueAsLong(false);
	}

	public Boolean valueAsBoolean() {
		return valueAsBoolean(false);
	}

	public List<String> valueAsArray() {
		return valueAsArray(false);
	}

	public Map<String,String> valueAsMap() {
		return valueAsMap(false);
	}

	/**
	 * Convert to JSON object
	 * @return JSON Object
	 */
	public JsonNode toJson() {
		ObjectNode json = (ObjectNode) definition.toJson();
		putValue(json, "value", definition.getType(), false);

		return json;
	}

	/**
	 * Convert to JSON object in format for DB
	 * @return JSON Object
	 */
	public JsonNode toJsonForDb() {
		ObjectNode obj = JsonNodeFactory.instance.objectNode();
		obj.put("type", definition.getType());
		putValue(obj, "oldValue", definition.getType(), true);
		putValue(obj, "newValue", definition.getType(), false);
		obj.put("comment", comment);

		return obj;
	}

	/**
	 * Parse from JSON obtained from Perun
	 * @param json JSON from Perun
	 * @return PerunAttribute or null
	 */
	public static PerunAttribute fromJsonOfPerun(JsonNode json) {
		if (Utils.checkParamsInvalid(json)) {
			throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
		}

		PerunAttributeDefinition definition = PerunAttributeDefinition.fromPerunJson(json);

		Object value = getValue(json, "value", definition.getType());
		PerunAttribute attr = new PerunAttribute();
		attr.setDefinition(definition);
		attr.setValue(value);

		return attr;
	}

	/**
	 * Parse from JSON obtained from DB
	 * @param name name attribute (full urn)
	 * @param json JSON from DB
	 * @param attributeDefinitionMap map containing attribute definitions
	 * @param inputMap map containing inputs
	 * @return PerunAttribute or null
	 */
	public static PerunAttribute fromJsonOfDb(String name, JsonNode json, Map<String,
			PerunAttributeDefinition> attributeDefinitionMap, Map<String, AttrInput> inputMap) {
		if (name == null || name.isEmpty() ||
				json == null || json.isEmpty() ||
				attributeDefinitionMap == null || attributeDefinitionMap.isEmpty() ||
				inputMap == null || inputMap.isEmpty() ||
				!attributeDefinitionMap.containsKey(name) || ! inputMap.containsKey(name)) {
			return null;
		}

		String type = json.get("type").textValue();
		Object newValue = getValue(json, "newValue", type);
		Object oldValue = getValue(json, "oldValue", type);
		String comment = json.hasNonNull("comment") ? json.get("comment").textValue() : null;

		PerunAttributeDefinition def = attributeDefinitionMap.get(name);
		AttrInput input = inputMap.get(name);

		return new PerunAttribute(def, name, newValue, oldValue, comment, input);
	}

	@Override
	public String toString() {
		return "PerunAttribute{" +
				"value=" + value +
				", oldValue=" + oldValue +
				", fullName='" + fullName + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PerunAttribute that = (PerunAttribute) o;
		return Objects.equals(value, that.value) &&
				Objects.equals(oldValue, that.oldValue) &&
				Objects.equals(comment, that.comment) &&
				Objects.equals(fullName, that.fullName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value, oldValue, comment, fullName);
	}

	private String valueAsString(boolean isOldValue) {
		if (isOldValue) {
			return valueAsString(oldValue);
		} else {
			return valueAsString(value);
		}
	}

	private Long valueAsLong(boolean isOldValue) {
		if (isOldValue) {
			return valueAsLong(oldValue);
		} else {
			return valueAsLong(value);
		}
	}

	private Boolean valueAsBoolean(boolean isOldValue) {
		if (isOldValue) {
			return valueAsBoolean(oldValue);
		} else {
			return valueAsBoolean(value);
		}
	}

	private List<String> valueAsArray(boolean isOldValue) {
		if (isOldValue) {
			return valueAsArray(oldValue);
		} else {
			return valueAsArray(value);
		}
	}

	private Map<String,String> valueAsMap(boolean isOldValue) {
		if (isOldValue) {
			return valueAsMap(oldValue);
		} else {
			return valueAsMap(value);
		}
	}

	private void putValue(ObjectNode json, String key, String type, boolean isOldValue) {
		JsonNode value = null;
		switch (type) {
			case STRING_TYPE:
			case LARGE_STRING_TYPE:
				value = JsonNodeFactory.instance.textNode(valueAsString(isOldValue));
				break;
			case INTEGER_TYPE:
				value = JsonNodeFactory.instance.numberNode(valueAsLong(isOldValue));
				break;
			case BOOLEAN_TYPE:
				value = JsonNodeFactory.instance.booleanNode(valueAsBoolean(isOldValue));
				break;
			case ARRAY_TYPE:
			case LARGE_ARRAY_LIST_TYPE:
				List<String> arrValue = valueAsArray(isOldValue);
				if (arrValue != null) {
					ArrayNode arr = JsonNodeFactory.instance.arrayNode();
					for (String sub: arrValue) {
						arr.add(sub);
					}
					value = arr;
				}

				break;
			case MAP_TYPE:
				Map<String, String> mapValue = valueAsMap(isOldValue);
				if (mapValue != null) {
					ObjectNode obj = JsonNodeFactory.instance.objectNode();
					for (Map.Entry<String, String> sub: mapValue.entrySet()) {
						obj.put(sub.getKey(), sub.getValue());
					}
					value = obj;
				}
				break;
		}

		if (value == null) {
			json.set(key, JsonNodeFactory.instance.nullNode());
		} else {
			json.set(key, value);
		}
	}

	private static Object getValue(JsonNode json, String key, String type) {
		if (json.get(key) instanceof NullNode && BOOLEAN_TYPE.equals(type)) {
			return false;
		}
		if (json.get(key) instanceof NullNode) {
			return null;
		}
		switch (type) {
			case STRING_TYPE:
			case LARGE_STRING_TYPE:
				return json.get(key).textValue();
			case INTEGER_TYPE:
				return json.get(key).asLong();
			case BOOLEAN_TYPE:
				return json.get(key).asBoolean();
			case ARRAY_TYPE:
			case LARGE_ARRAY_LIST_TYPE:
				ArrayNode arr = (ArrayNode) json.get(key);
				List<String> arrValue = new ArrayList<>();

				for (int i = 0; i < arr.size(); i++) {
					arrValue.add(arr.get(i).textValue());
				}

				return arrValue;
			case MAP_TYPE:
				ObjectNode obj = (ObjectNode) json.get(key);
				Map<String, String> mapValue = new LinkedHashMap<>();

				Iterator<String> keys = obj.fieldNames();
				while (keys.hasNext()) {
					String mapKey = keys.next();
					mapValue.put(mapKey, obj.get(mapKey).textValue());
				}

				return mapValue;
		}
		return null;
	}

	private String valueAsString(Object value) {
		if (value instanceof String) {
			return (String) value;
		}

		return null;
	}

	private Long valueAsLong(Object value) {
		if (value instanceof Long) {
			return (Long) value;
		}

		return null;
	}

	private Boolean valueAsBoolean(Object value) {
		if (value instanceof Boolean) {
			return (Boolean) value;
		} else if (value == null) {
			return false;
		}

		return false;
	}

	private List<String> valueAsArray(Object value) {
		if (value instanceof List) {
			List<String> result = new ArrayList<>();
			List val = (List) value;
			for (Object o: val) {
				if (!(o instanceof String)) {
					return null;
				} else {
					result.add((String) o);
				}
			}
			return result;
		}

		return null;
	}

	private Map<String, String> valueAsMap(Object value) {
		if (value instanceof Map) {
			Map<String,String> result = new LinkedHashMap<>();
			Map val = (Map) value;
			for (Object entry: val.entrySet()) {
				if (!(entry instanceof Map.Entry)) {
					return null;
				} else {
					Map.Entry ent = (Map.Entry) entry;
					if (!(ent.getKey() instanceof String) || ! (ent.getValue() instanceof String)) {
						return null;
					} else {
						String key = (String) ent.getKey();
						String pair = (String) ent.getValue();
						result.put(key, pair);
					}
				}
			}
			return result;
		}

		return null;
	}
}
