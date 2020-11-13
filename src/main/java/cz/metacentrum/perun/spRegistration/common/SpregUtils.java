package cz.metacentrum.perun.spRegistration.common;

import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttributeDefinition;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

public class SpregUtils {

    public static Map<AttributeCategory, Map<String, PerunAttribute>> filterInvalidAttributes(
            @NonNull Map<AttributeCategory, Map<String, PerunAttribute>> attributes,
            @NonNull Map<String, PerunAttributeDefinition> definitionMap)
    {
        if (attributes == null) {
            return null;
        } else if (attributes.isEmpty()) {
            return attributes;
        }

        Map<AttributeCategory, Map<String, PerunAttribute>> valid = new HashMap<>();
        for (Map.Entry<AttributeCategory, Map<String, PerunAttribute>> categoryMapEntry : attributes.entrySet()) {
            AttributeCategory category = categoryMapEntry.getKey();
            Map<String, PerunAttribute> attributeMap = categoryMapEntry.getValue();
            Map<String, PerunAttribute> validInCategory = new HashMap<>();
            for (String attrName : attributeMap.keySet()) {
                if (definitionMap.containsKey(attrName)) {
                    validInCategory.put(attrName, attributeMap.get(attrName));
                }
            }
            valid.put(category, validInCategory);
        }

        return valid;

    }

}
