package cz.metacentrum.perun.spRegistration.persistence.adapters;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import org.springframework.util.StringUtils;

public class AdapterUtils {

    public static ObjectNode generateFacilityJson(@NonNull String name, @NonNull String description) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Name of Facility cannot be NULL nor EMPTY");
        }

        ObjectNode facilityJson = JsonNodeFactory.instance.objectNode();
        facilityJson.set("id", JsonNodeFactory.instance.nullNode());
        facilityJson.put("name", name);
        facilityJson.put("description", description);
        return facilityJson;
    }

}
