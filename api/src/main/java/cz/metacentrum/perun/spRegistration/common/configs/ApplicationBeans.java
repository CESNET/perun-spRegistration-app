package cz.metacentrum.perun.spRegistration.common.configs;

import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import javax.validation.constraints.NotNull;
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
    private final SecretKeySpec secretKeySpec;

    @Autowired
    public ApplicationBeans(ApplicationProperties applicationProperties, PerunAdapter perunAdapter)
            throws PerunUnknownException, PerunConnectionException, NoSuchAlgorithmException {
        this.applicationProperties = applicationProperties;
        this.perunAdapter = perunAdapter;

        List<String> attrNames = applicationProperties.getAttributesProperties().getAttrNames();
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

    public AttributeCategory getAttrCategory(@NotNull String attrFullName) {
        return attributeCategoryMap.get(attrFullName);
    }

    public PerunAttributeDefinition getAttrDefinition(@NotNull String attrFullName) {
        return attributeDefinitionMap.get(attrFullName);
    }
}
