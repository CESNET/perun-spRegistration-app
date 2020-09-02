package cz.metacentrum.perun.spRegistration.common.configs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.models.AttrInput;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttributeDefinition;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AppBeans {

    @Bean("attributeDefinitionMap")
    public Map<String, PerunAttributeDefinition> attributeDefinitionMap() {
        return new HashMap<>();
    }

    @Bean("attributeCategoryMap")
    public Map<String, AttributeCategory> attributeCategoryMap() {
        return new HashMap<>();
    }

    @Bean("attrInputMap")
    public Map<String, AttrInput> attrInputMap() {
        return new HashMap<>();
    }

    @Bean("serviceInputs")
    @Autowired
    public List<AttrInput> serviceInputs(@NonNull AttributesProperties attributesProperties) {
        return this.initInputs(attributesProperties.getServiceAttributesConfig());
    }

    @Bean("organizationInputs")
    @Autowired
    public List<AttrInput> organizationInputs(@NonNull AttributesProperties attributesProperties) {
        return this.initInputs(attributesProperties.getOrganizationAttrsConfig());
    }

    @Bean("samlInputs")
    @Autowired
    public List<AttrInput> samlInputs(@NonNull AttributesProperties attributesProperties) {
        return this.initInputs(attributesProperties.getSamlAttrsConfig());
    }

    @Bean("oidcInputs")
    @Autowired
    public List<AttrInput> oidcInputs(@NonNull AttributesProperties attributesProperties) {
        return this.initInputs(attributesProperties.getOidcAttrsConfig());
    }

    @Bean("membershipInputs")
    @Autowired
    public List<AttrInput> membershipInputs(@NonNull AttributesProperties attributesProperties) {
        return this.initInputs(attributesProperties.getAcAttrsConfig());
    }

    private List<AttrInput> getInputsFromYaml(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(new File(path), new TypeReference<List<AttrInput>>() {});
    }


    private List<AttrInput> initInputs(String config) {
        try {
            return this.getInputsFromYaml(config);
        } catch (IOException e) {
            log.error("Caught IOException when initializing facility attributes - saml", e);
            throw new IllegalArgumentException("Error when reading file: " + config);
        }
    }

}
