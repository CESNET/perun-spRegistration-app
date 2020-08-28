package cz.metacentrum.perun.spRegistration.common.configs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.models.AttrInput;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Getter
@Slf4j
public class ApplicationBeans {

    @NotNull private final ApplicationProperties applicationProperties;
    @NotNull private final PerunAdapter perunAdapter;
    private final Map<String, PerunAttributeDefinition> attributeDefinitionMap = new HashMap<>();
    private final Map<String, AttributeCategory> attributeCategoryMap = new HashMap<>();
    private final AttributesProperties attributesProperties;
    private final SecretKeySpec secretKeySpec;

    @Autowired
    public ApplicationBeans(ApplicationProperties applicationProperties,
                            PerunAdapter perunAdapter,
                            AttributesProperties attributesProperties)
            throws PerunUnknownException, PerunConnectionException, NoSuchAlgorithmException
    {
        this.applicationProperties = applicationProperties;
        this.perunAdapter = perunAdapter;
        this.attributesProperties = attributesProperties;

        List<String> attrNames = this.attributesProperties.getAttrNames();
        for (String attrName: attrNames) {
            PerunAttributeDefinition def = perunAdapter.getAttributeDefinition(attrName);
            if (def != null) {
                attributeDefinitionMap.put(attrName, def);
            } else {
                log.error("Null attribute definition for attribute name: {}!", attrName);
                throw new IllegalStateException("Cannot initialize attribute definition for name " + attrName);
            }
        }

        this.secretKeySpec = this.generateSecretKeySpec(applicationProperties.getSecretKey());
    }

    @Bean("facilityServiceConfig")
    public AttrsConfig facilityServiceConfig(AttributesProperties attributesProperties)
            throws PerunUnknownException, PerunConnectionException
    {
        String config = attributesProperties.getServiceAttributesConfig();
        try {
            List<AttrInput> inputs = this.getInputsFromYaml(config);
            return new AttrsConfig(perunAdapter, inputs, AttributeCategory.SERVICE, attributeDefinitionMap, attributeCategoryMap, applicationProperties);
        } catch (IOException e) {
            log.error("Caught IOException when initializing facility attributes - service", e);
            throw new IllegalArgumentException("Error when reading file: " + config);
        }
    }

    @Bean("facilityOrganizationConfig")
    public AttrsConfig facilityOrganizationConfig(AttributesProperties attributesProperties)
            throws PerunUnknownException, PerunConnectionException
    {
        String config = attributesProperties.getOidcAttrsConfig();
        try {
            List<AttrInput> inputs = this.getInputsFromYaml(config);
            return new AttrsConfig(perunAdapter, inputs, AttributeCategory.ORGANIZATION, attributeDefinitionMap, attributeCategoryMap, applicationProperties);
        } catch (IOException e) {
            log.error("Caught IOException when initializing facility attributes - organization", e);
            throw new IllegalArgumentException("Error when reading file: " + config);
        }
    }

    @Bean("facilitySamlConfig")
    public AttrsConfig facilitySamlConfig(AttributesProperties attributesProperties)
            throws PerunUnknownException, PerunConnectionException
    {
        String config = attributesProperties.getSamlAttrsConfig();
        try {
            List<AttrInput> inputs = this.getInputsFromYaml(config);
            return new AttrsConfig(perunAdapter, inputs, AttributeCategory.PROTOCOL, attributeDefinitionMap, attributeCategoryMap, applicationProperties);
        } catch (IOException e) {
            log.error("Caught IOException when initializing facility attributes - saml", e);
            throw new IllegalArgumentException("Error when reading file: " + config);
        }
    }

    @Bean("facilityOidcConfig")
    public AttrsConfig facilityOidcConfig(AttributesProperties attributesProperties)
            throws PerunUnknownException, PerunConnectionException
    {
        String config = attributesProperties.getOidcAttrsConfig();
        try {
            List<AttrInput> inputs = this.getInputsFromYaml(config);
            return new AttrsConfig(perunAdapter, inputs, AttributeCategory.PROTOCOL, attributeDefinitionMap, attributeCategoryMap, applicationProperties);
        } catch (IOException e) {
            log.error("Caught IOException when initializing facility attributes - oidc", e);
            throw new IllegalArgumentException("Error when reading file: " + config);
        }
    }

    @Bean("facilityAccessControlConfig")
    public AttrsConfig facilityAccessControlConfig(AttributesProperties attributesProperties)
            throws PerunUnknownException, PerunConnectionException
    {
        String config = attributesProperties.getAcAttrsConfig();
        try {
            List<AttrInput> inputs = this.getInputsFromYaml(config);
            return new AttrsConfig(perunAdapter, inputs, AttributeCategory.ACCESS_CONTROL, attributeDefinitionMap, attributeCategoryMap, applicationProperties);
        } catch (IOException e) {
            log.error("Caught IOException when initializing facility attributes - access control", e);
            throw new IllegalArgumentException("Error when reading file: " + config);
        }
    }

    public AttributeCategory getAttrCategory(@NotNull String attrFullName) {
        return attributeCategoryMap.get(attrFullName);
    }

    public PerunAttributeDefinition getAttrDefinition(@NotNull String attrFullName) {
        return attributeDefinitionMap.get(attrFullName);
    }

    // private methods

    private SecretKeySpec generateSecretKeySpec(String secret) throws NoSuchAlgorithmException {
        secret = fixSecret(secret);
        MessageDigest sha;
        byte[] key = secret.getBytes(StandardCharsets.UTF_8);
        sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        return new SecretKeySpec(key, "AES");
    }

    private String fixSecret(String s) {
        if (s.length() < 32) {
            int missingLength = 32 - s.length();
            StringBuilder sBuilder = new StringBuilder(s);
            for (int i = 0; i < missingLength; i++) {
                sBuilder.append('A');
            }
            s = sBuilder.toString();
        }
        return s.substring(0, 32);
    }

    private List<AttrInput> getInputsFromYaml(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(new File(path), new TypeReference<List<AttrInput>>() {});
    }

}
