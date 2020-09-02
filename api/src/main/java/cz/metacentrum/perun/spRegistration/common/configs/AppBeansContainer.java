package cz.metacentrum.perun.spRegistration.common.configs;

import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.models.AttrInput;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @NonNull private final List<AttrInput> serviceInputs;
    @NonNull private final List<AttrInput> organizationInputs;
    @NonNull private final List<AttrInput> membershipInputs;
    @NonNull private final List<AttrInput> oidcInputs;
    @NonNull private final List<AttrInput> samlInputs;
    @NonNull private final Map<String, AttrInput> attrInputMap;

    @Autowired
    public AppBeansContainer(ApplicationProperties applicationProperties,
                             PerunAdapter perunAdapter,
                             Map<String, PerunAttributeDefinition> attributeDefinitionMap,
                             Map<String, AttributeCategory> attributeCategoryMap,
                             Map<String, AttrInput> attrInputMap,
                             AttributesProperties attributesProperties,
                             @Qualifier("serviceInputs") List<AttrInput> serviceInputs,
                             @Qualifier("organizationInputs") List<AttrInput> organizationInputs,
                             @Qualifier("membershipInputs") List<AttrInput> membershipInputs,
                             @Qualifier("oidcInputs") List<AttrInput> oidcInputs,
                             @Qualifier("samlInputs") List<AttrInput> samlInputs)
            throws PerunUnknownException, PerunConnectionException, NoSuchAlgorithmException
    {
        this.applicationProperties = applicationProperties;
        this.perunAdapter = perunAdapter;
        this.attributeDefinitionMap = attributeDefinitionMap;
        this.attributeCategoryMap = attributeCategoryMap;
        this.attrInputMap = attrInputMap;
        this.attributesProperties = attributesProperties;
        this.serviceInputs = serviceInputs;
        this.organizationInputs = organizationInputs;
        this.membershipInputs = membershipInputs;
        this.oidcInputs = oidcInputs;
        this.samlInputs = samlInputs;

        this.initializeDefinitions();
        this.initInputs(serviceInputs, AttributeCategory.SERVICE);
        this.initInputs(organizationInputs, AttributeCategory.ORGANIZATION);
        this.initInputs(membershipInputs, AttributeCategory.ACCESS_CONTROL);
        this.initInputs(oidcInputs, AttributeCategory.PROTOCOL);
        this.initInputs(samlInputs, AttributeCategory.PROTOCOL);

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

}
