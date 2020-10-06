package cz.metacentrum.perun.spRegistration.persistence.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.metacentrum.perun.spRegistration.persistence.enums.ServiceEnvironment;
import cz.metacentrum.perun.spRegistration.persistence.enums.ServiceProtocol;

import java.util.Map;

public class ProvidedService {

    private Long id;
    private Long facilityId;
    private ServiceProtocol protocol;
    private ServiceEnvironment environment;
    private Map<String, String> name;
    private Map<String, String> description;
    private String identifier;

    public ProvidedService() { }

    public ProvidedService(Long id, Long facilityId, ServiceProtocol protocol, ServiceEnvironment environment,
                           Map<String, String> name, Map<String, String> description, String identifier) {
        this.id = id;
        this.facilityId = facilityId;
        this.protocol = protocol;
        this.environment = environment;
        this.name = name;
        this.description = description;
        this.identifier = identifier;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Long facilityId) {
        this.facilityId = facilityId;
    }

    public ServiceProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(ServiceProtocol protocol) {
        this.protocol = protocol;
    }

    public ServiceEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(ServiceEnvironment environment) {
        this.environment = environment;
    }

    public Map<String, String> getName() {
        return name;
    }

    public void setName(Map<String, String> name) {
        this.name = name;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    public String nameAsJsonString() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this.name);
    }

    public String descriptionAsJsonString() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this.description);
    }

    public void nameFromDbJson(String nameJson) throws JsonProcessingException {
        this.name = new ObjectMapper().readValue(nameJson, Map.class);
    }

    public void descriptionFromDbJson(String descriptionJson) throws JsonProcessingException {
        this.description = new ObjectMapper().readValue(descriptionJson, Map.class);
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
