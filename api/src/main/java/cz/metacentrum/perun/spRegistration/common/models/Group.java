package cz.metacentrum.perun.spRegistration.common.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Objects;

public class Group extends PerunEntity {

    private String name;
    private String shortName;
    private String description;
    private Long parentGroupId;
    private Long voId;
    private final String beanName = "Group";

    public Group(Long id, String name, String shortName, String description, Long parentGroupId, Long voId) {
        super(id);
        this.name = name;
        this.shortName = shortName;
        this.description = description;
        this.parentGroupId = parentGroupId;
        this.voId = voId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBeanName() {
        return beanName;
    }

    public Long getParentGroupId() {
        return parentGroupId;
    }

    public void setParentGroupId(Long parentGroupId) {
        this.parentGroupId = parentGroupId;
    }

    public Long getVoId() {
        return voId;
    }

    public void setVoId(Long voId) {
        this.voId = voId;
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

    @Override
    public String toString() {
        return "Group{" +
                "name='" + name + '\'' +
                ", shortName='" + shortName + '\'' +
                ", description='" + description + '\'' +
                ", parentGroupId=" + parentGroupId +
                ", voId=" + voId +
                ", beanName='" + beanName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return Objects.equals(name, group.name) &&
                Objects.equals(shortName, group.shortName) &&
                Objects.equals(description, group.description) &&
                Objects.equals(parentGroupId, group.parentGroupId) &&
                Objects.equals(voId, group.voId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, shortName, description, parentGroupId, voId);
    }
}
