package cz.metacentrum.perun.spRegistration.common.configs;

import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.models.AttrInput;
import cz.metacentrum.perun.spRegistration.common.models.InputsContainer;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Getter
@Slf4j
public class AppBeansContainer {

    @NonNull private final ApplicationProperties applicationProperties;
    @NonNull private final PerunAdapter perunAdapter;
    @NonNull private final Map<String, PerunAttributeDefinition> attributeDefinitionMap;
    @NonNull private final Map<String, AttributeCategory> attributeCategoryMap;
    @NonNull private final AttributesProperties attributesProperties;
    @NonNull private final SecretKeySpec secretKeySpec;
    @NonNull private final Map<String, AttrInput> attrInputMap;

    @Autowired
    public AppBeansContainer(@NonNull ApplicationProperties applicationProperties,
                             @NonNull PerunAdapter perunAdapter,
                             @NonNull Map<String, PerunAttributeDefinition> attributeDefinitionMap,
                             @NonNull Map<String, AttributeCategory> attributeCategoryMap,
                             @NonNull Map<String, AttrInput> attrInputMap,
                             @NonNull AttributesProperties attributesProperties,
                             @NonNull InputsContainer inputsContainer)
            throws PerunUnknownException, PerunConnectionException, NoSuchAlgorithmException
    {
        this.applicationProperties = applicationProperties;
        this.perunAdapter = perunAdapter;
        this.attributeDefinitionMap = attributeDefinitionMap;
        this.attributeCategoryMap = attributeCategoryMap;
        this.attrInputMap = attrInputMap;
        this.attributesProperties = attributesProperties;

        this.initializeDefinitions();
        this.initInputs(inputsContainer.getServiceInputs(), AttributeCategory.SERVICE);
        this.initInputs(inputsContainer.getOrganizationInputs(), AttributeCategory.ORGANIZATION);
        this.initInputs(inputsContainer.getMembershipInputs(), AttributeCategory.ACCESS_CONTROL);
        this.initInputs(inputsContainer.getSamlInputs(), AttributeCategory.PROTOCOL);
        this.initInputs(inputsContainer.getOidcInputs(), AttributeCategory.PROTOCOL);

        this.secretKeySpec = this.generateSecretKeySpec(applicationProperties.getSecretKey());
    }

    private void initializeDefinitions() throws PerunUnknownException, PerunConnectionException {
        List<String> attrNames = this.attributesProperties.getAttrNames();
        for (String attrName: attrNames) {
            PerunAttributeDefinition def = perunAdapter.getAttributeDefinition(attrName);
            if (def != null) {
                this.attributeDefinitionMap.put(attrName, def);
            } else {
                log.error("Null attribute definition for attribute name: {}!", attrName);
                throw new IllegalStateException("Cannot initialize attribute definition for name " + attrName);
            }
        }
    }

    private void initInputs(@NonNull List<AttrInput> attrInputs, AttributeCategory category)
            throws PerunUnknownException, PerunConnectionException
    {
        for (AttrInput a: attrInputs) {
            PerunAttributeDefinition definition = perunAdapter.getAttributeDefinition(a.getName());
            attrInputMap.put(a.getName(), a);
            attributeCategoryMap.put(a.getName(), category);
            attributeDefinitionMap.put(a.getName(), definition);
        }
    }

    public AttributeCategory getAttrCategory(@NonNull String attrFullName) {
        return attributeCategoryMap.get(attrFullName);
    }

    public PerunAttributeDefinition getAttrDefinition(@NonNull String attrFullName) {
        return attributeDefinitionMap.get(attrFullName);
    }

    public Set<String> getAllAttrNames() {
        return attributeDefinitionMap.keySet();
    }

    // private methods

    private SecretKeySpec generateSecretKeySpec(@NonNull String secret) throws NoSuchAlgorithmException {
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

}
