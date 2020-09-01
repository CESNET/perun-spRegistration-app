package cz.metacentrum.perun.spRegistration.common.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import cz.metacentrum.perun.spRegistration.common.exceptions.InconvertibleValueException;
import cz.metacentrum.perun.spRegistration.persistence.mappers.MapperUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Representation of attribute from Perun.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@Getter
@Setter
@ToString(exclude = {"definition", "input"})
@EqualsAndHashCode(exclude = {"definition", "input"})
@NoArgsConstructor
public class PerunAttribute {

	public final static String STRING_TYPE = "java.lang.String";
	public final static String INTEGER_TYPE = "java.lang.Integer";
	public final static String BOOLEAN_TYPE = "java.lang.Boolean";
	public final static String ARRAY_TYPE = "java.util.ArrayList";
	public final static String MAP_TYPE = "java.util.LinkedHashMap";
	public final static String LARGE_STRING_TYPE = "java.lang.LargeString";
	public final static String LARGE_ARRAY_LIST_TYPE = "java.util.LargeArrayList";

	private PerunAttributeDefinition definition;
	private JsonNode value;
	private JsonNode oldValue;
	private String comment;
	private String fullName;
	private AttrInput input;

	public PerunAttribute(PerunAttributeDefinition definition, String fullName, JsonNode value, JsonNode oldValue,
						  String comment, AttrInput input)
	{
		this.definition = definition;
		this.fullName = fullName;
		this.value = value;
		if (definition != null && BOOLEAN_TYPE.equals(definition.getType())) {
			if (value == null) {
				this.value = JsonNodeFactory.instance.booleanNode(false);
			}
		}
		this.oldValue = oldValue;
		if (definition != null && BOOLEAN_TYPE.equals(definition.getType())) {
			if (oldValue == null) {
				this.value = JsonNodeFactory.instance.booleanNode(false);
			}
		}
		this.comment = comment;
		this.input = input;
	}

	public PerunAttribute(PerunAttributeDefinition attributeDefinition, JsonNode value) {
		this.definition = attributeDefinition;
		this.fullName = attributeDefinition.getFullName();
		this.value = value;
		if (BOOLEAN_TYPE.equals(definition.getType())) {
			if (value == null) {
				this.value = JsonNodeFactory.instance.booleanNode(false);
			}
		}
		this.oldValue = null;
		this.comment = null;
		this.input = null;
	}

	public String getFullName() {
		return (definition != null && fullName == null) ? definition.getFullName() : fullName;
	}

	/**
	 * Convert to JSON object
	 * @return JSON Object
	 */
	public JsonNode toJson() {
		ObjectNode json = (ObjectNode) definition.toJson();
		json.set(MapperUtils.VALUE, value);

		return json;
	}

	/**
	 * Convert to JSON object in format for DB
	 * @return JSON Object
	 */
	public JsonNode toJsonForDb() {
		ObjectNode obj = JsonNodeFactory.instance.objectNode();
		obj.put("type", definition.getType());
		obj.set("oldValue", this.valueAsJson(true));
		obj.set("newValue", this.valueAsJson(false));
		obj.put("comment", comment);

		return obj;
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

		JsonNode newValue = json.get("newValue");
		JsonNode oldValue = json.get("oldValue");
		String comment = json.hasNonNull("comment") ? json.get("comment").textValue() : null;

		PerunAttributeDefinition def = attributeDefinitionMap.get(name);
		AttrInput input = inputMap.get(name);

		return new PerunAttribute(def, name, newValue, oldValue, comment, input);
	}

	public void setValue(@NonNull String type, JsonNode value) {
		if (PerunAttribute.isNullValue(value)) {
			if (!BOOLEAN_TYPE.equals(type)) {
				this.value = JsonNodeFactory.instance.nullNode();
				return;
			} else {
				value = JsonNodeFactory.instance.booleanNode(false);
			}
		}

		this.value = value;
	}

	public String valueAsString() {
		return this.valueAsString(false);
	}

	public Long valueAsLong() {
		return this.valueAsLong(false);
	}

	public Boolean valueAsBoolean() {
		return this.valueAsBoolean(false);
	}

	public List<String> valueAsList() {
		return this.valueAsList(false);
	}

	public Map<String, String> valueAsMap() {
		return this.valueAsMap(false);
	}

	public String oldValueAsString() {
		return this.valueAsString(true);
	}

	public Long oldValueAsLong() {
		return this.valueAsLong(true);
	}

	public Boolean oldValueAsBoolean() {
		return this.valueAsBoolean(true);
	}

	public List<String> oldValueAsList() {
		return this.valueAsList(true);
	}

	public Map<String, String> oldValueAsMap() {
		return this.valueAsMap(true);
	}

	/**
	 * Get value as String.
	 *
	 * @return String value or null.
	 */
	private String valueAsString(boolean old) {
		JsonNode value = old ? this.oldValue : this.value;
		if ((STRING_TYPE.equals(definition.getType()) || LARGE_STRING_TYPE.equals(definition.getType()))) {
			if (value == null || value instanceof NullNode) {
				return null;
			} else if (value instanceof TextNode) {
				return value.textValue();
			}
		}

		return value.asText();
	}

	private Long valueAsLong(boolean old) {
		JsonNode value = old ? this.oldValue : this.value;
		if (INTEGER_TYPE.equals(definition.getType())) {
			if (PerunAttribute.isNullValue(value)) {
				return null;
			} else if (value instanceof NumericNode) {
				return value.longValue();
			}
		}

		throw inconvertible(Long.class.getName());
	}

	private boolean valueAsBoolean(boolean old) {
		JsonNode value = old ? this.oldValue : this.value;
		if (BOOLEAN_TYPE.equals(definition.getType())) {
			if (value == null || value instanceof NullNode) {
				return false;
			} else if (value instanceof BooleanNode) {
				return value.asBoolean();
			}
		}

		throw inconvertible(Boolean.class.getName());
	}

	private List<String> valueAsList(boolean old) {
		JsonNode value = old ? this.oldValue : this.value;
		List<String> arr = new ArrayList<>();
		if ((ARRAY_TYPE.equals(definition.getType()) || LARGE_ARRAY_LIST_TYPE.equals(definition.getType()))) {
			if (PerunAttribute.isNullValue(value)) {
				return null;
			} else if (value instanceof ArrayNode) {
				ArrayNode arrJson = (ArrayNode) value;
				arrJson.forEach(item -> arr.add(item.asText()));
			}
		} else {
			arr.add(valueAsString());
		}

		return arr;
	}

	private Map<String, String> valueAsMap(boolean old) throws InconvertibleValueException {
		JsonNode value = old ? this.oldValue : this.value;
		if (MAP_TYPE.equals(definition.getType())) {
			if (PerunAttribute.isNullValue(value)) {
				return new HashMap<>();
			} else if (value instanceof ObjectNode) {
				Map<String, String> res = new HashMap<>();
				ObjectNode objJson = (ObjectNode) value;
				Iterator<String> it = objJson.fieldNames();
				while (it.hasNext()) {
					String key = it.next();
					res.put(key, objJson.get(key).asText());
				}
				return res;
			}
		}

		throw inconvertible(Map.class.getName());
	}

	private JsonNode valueAsJson(boolean old) {
		return old ? this.oldValue : this.value;
	}

	private InconvertibleValueException inconvertible(String clazzName) {
		return new InconvertibleValueException("Cannot convert value of attribute to " + clazzName +
				" for object: " + this.toString());
	}

	private static boolean isNullValue(JsonNode value) {
		return value == null ||
				value instanceof NullNode ||
				value.isNull() ||
				"null".equalsIgnoreCase(value.asText());
	}

}
