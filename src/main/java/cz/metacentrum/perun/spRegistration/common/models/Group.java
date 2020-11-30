package cz.metacentrum.perun.spRegistration.common.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class Group extends PerunEntity {

    @NonNull private String name;
    @NonNull private String shortName;
    @NonNull private String description;
    private Long parentGroupId;
    @NonNull private Long voId;

    private final String beanName = "Group";

    public Group(String name, String shortName, String description, Long parentGroupId, Long voId) {
        this.setName(name);
        this.setShortName(shortName);
        this.setDescription(description);
        this.setParentGroupId(parentGroupId);
        this.setVoId(voId);
    }

    public Group(Long id, String name, String shortName, String description, Long parentGroupId, Long voId) {
        this(name,shortName, description, parentGroupId, voId);
        this.setId(id);
    }

    public void setName(@NonNull String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("name cannot be empty");
        }
        this.name = name;
    }

    public JsonNode toJson() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("id", this.getId());
        node.put("shortName", shortName);
        node.put("name", name);
        node.put("description", description);
        node.put("beanName", beanName);
        node.put("parentGroupId", parentGroupId);
        node.put("voId", voId);

        return node;
    }

}
