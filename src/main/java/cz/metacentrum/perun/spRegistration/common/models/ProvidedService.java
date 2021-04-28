package cz.metacentrum.perun.spRegistration.common.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.metacentrum.perun.spRegistration.persistence.enums.ServiceEnvironment;
import cz.metacentrum.perun.spRegistration.persistence.enums.ServiceProtocol;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.util.Map;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class ProvidedService {

    @NonNull private Long id;
    @NonNull private Long facilityId;
    @NonNull private ServiceProtocol protocol;
    @NonNull private ServiceEnvironment environment;
    @NonNull private Map<String, String> name;
    @NonNull private Map<String, String> description;
    @NonNull private String identifier;
    private boolean facilityDeleted = false;

    public ProvidedService(@NonNull Long id,
                           @NonNull Long facilityId,
                           @NonNull ServiceProtocol protocol,
                           @NonNull ServiceEnvironment environment,
                           @NonNull Map<String, String> name,
                           @NonNull Map<String, String> description,
                           @NonNull String identifier,
                           boolean facilityDeleted)
    {
        this.setId(id);
        this.setFacilityId(facilityId);
        this.setProtocol(protocol);
        this.setEnvironment(environment);
        this.setName(name);
        this.setDescription(description);
        this.setIdentifier(identifier);
        this.facilityDeleted = facilityDeleted;
    }

    public void setIdentifier(@NonNull String identifier) {
        if (!StringUtils.hasText(identifier)) {
            throw new IllegalArgumentException("Identifier cannot be null nor empty");
        }
        this.identifier = identifier;
    }

    public String nameAsJsonString() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this.name);
    }

    public String descriptionAsJsonString() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this.description);
    }

    public void nameFromDbJson(String nameJson) throws JsonProcessingException {
        this.name = new ObjectMapper().readValue(nameJson, new TypeReference<Map<String, String>>() {});
    }

    public void descriptionFromDbJson(String descriptionJson) throws JsonProcessingException {
        this.description = new ObjectMapper().readValue(descriptionJson, new TypeReference<Map<String, String>>() {});
    }

}
