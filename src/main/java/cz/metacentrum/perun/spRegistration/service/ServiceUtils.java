package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.common.configs.AppBeansContainer;
import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.common.enums.AttributeCategory;
import cz.metacentrum.perun.spRegistration.common.models.AttrInput;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.InputsContainer;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.RequestDTO;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Utility class containing methods for services.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@Slf4j
public class ServiceUtils {

	public static final String CIPHER_PARAMS = "AES/ECB/PKCS5PADDING";
	public static Cipher cipher;

	static {
		try {
			cipher = Cipher.getInstance(CIPHER_PARAMS);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}

	public static List<PerunAttribute> filterFacilityAttrs(Map<String, PerunAttribute> attrsMap, Set<String> toKeep) {
		if (attrsMap == null) {
			return null;
		} else if (attrsMap.isEmpty()) {
			return new ArrayList<>();
		} else if (toKeep == null) {
			return null;
		} else if (toKeep.isEmpty()) {
			return new ArrayList<>();
		}

		List<PerunAttribute> filteredAttributes = new ArrayList<>();
		for (String keptAttr: toKeep) {
			filteredAttributes.add(attrsMap.get(keptAttr));
		}

		return filteredAttributes;
	}

	public static boolean isOidcRequest(@NonNull RequestDTO request, @NonNull String entityIdAttr) {
		if (request.getAttributes() == null || request.getAttributes().get(AttributeCategory.PROTOCOL) == null) {
			throw new IllegalArgumentException("Request does not contain required category in attrs");
		}
		boolean isOidc = true;
		if (request.getAttributes().get(AttributeCategory.PROTOCOL).containsKey(entityIdAttr)) {
			isOidc = (request.getAttributes().get(AttributeCategory.PROTOCOL).get(entityIdAttr).getValue().isNull());
		}
		return isOidc;
	}

	public static boolean isOidcAttributes(@NonNull Map<String, PerunAttribute> attributes,
										   @NonNull String clientIdAttr)
	{
		boolean isOidc = false;
		if (attributes.containsKey(clientIdAttr)) {
			isOidc = !PerunAttribute.isEmptyValue(attributes.get(clientIdAttr).getValue());
		}
		return isOidc;
	}

	public static String encrypt(String strToEncrypt, @NonNull SecretKeySpec secretKeySpec)
			throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException
	{
		if (strToEncrypt == null || strToEncrypt.equalsIgnoreCase("null")) {
			return null;
		}
		Base64.Encoder b64enc = Base64.getUrlEncoder();
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
		return b64enc.encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
	}

	public static Set<String> getAttrNames(@NonNull InputsContainer inputsContainer, boolean isOidc,
										   @NonNull AttributesProperties attributesProperties)
	{
		Set<String> keptAttrs = new HashSet<>();

		keptAttrs.addAll(inputsContainer.getServiceInputs()
				.stream().map(AttrInput::getName).collect(Collectors.toList()));
		keptAttrs.addAll(inputsContainer.getOrganizationInputs()
				.stream().map(AttrInput::getName).collect(Collectors.toList()));
		keptAttrs.addAll(inputsContainer.getMembershipInputs()
				.stream().map(AttrInput::getName).collect(Collectors.toList()));

		if (isOidc) {
			keptAttrs.addAll(inputsContainer.getOidcInputs()
					.stream().map(AttrInput::getName).collect(Collectors.toList()));
			keptAttrs.add(attributesProperties.getNames().getOidcClientId());
			keptAttrs.add(attributesProperties.getNames().getOidcClientSecret());
		} else {
			keptAttrs.addAll(inputsContainer.getSamlInputs()
					.stream().map(AttrInput::getName).collect(Collectors.toList()));
		}
		return keptAttrs;
	}

	public static String decrypt(String strToDecrypt, @NonNull SecretKeySpec secretKeySpec)
			throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException
	{
		if (strToDecrypt == null || strToDecrypt.equalsIgnoreCase("null")) {
			return null;
		}

		Base64.Decoder b64dec = Base64.getUrlDecoder();
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
		return new String(cipher.doFinal(b64dec.decode(strToDecrypt)));
	}

	public static String generateClientId() {
		return UUID.randomUUID().toString();
	}

	public static String generateClientSecret() {
		String uuid = UUID.randomUUID().toString();
		uuid += UUID.randomUUID().toString();

		return uuid;
	}

	public static String getHash(@NonNull String input) {
		byte[] digest = DigestUtils.md5Digest(input.getBytes());
		StringBuilder hexString = new StringBuilder();
		for (byte b : digest) {
			String hex = Integer.toHexString(0xFF & b);
			if (hex.length() == 1) hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString().toUpperCase();
	}

	public static Map<String, String> extractFacilityName(@NonNull Facility facility,
														  @NonNull AttributesProperties attrsProps)
	{
		return ServiceUtils.extractFacilityName(facility.getAttributes().get(AttributeCategory.SERVICE),
				attrsProps);
	}

	public static Map<String, String> extractFacilityDescription(@NonNull Facility facility,
																 @NonNull AttributesProperties attrsProps)
	{
		return ServiceUtils.extractFacilityDescription(facility.getAttributes().get(AttributeCategory.SERVICE),
				attrsProps);
	}

	public static Map<String, String> extractFacilityName(@NonNull Map<String, PerunAttribute> attrs,
														  @NonNull AttributesProperties attributesProperties)
	{
		if (attrs.containsKey(attributesProperties.getNames().getServiceName())) {
			return attrs.get(attributesProperties.getNames().getServiceName()).valueAsMap();
		} else {
			return new HashMap<>();
		}
	}

	public static Map<String, String> extractFacilityDescription(@NonNull Map<String, PerunAttribute> attrs,
																 @NonNull AttributesProperties attributesProperties)
	{
		if (attrs.containsKey(attributesProperties.getNames().getServiceDesc())) {
			return attrs.get(attributesProperties.getNames().getServiceDesc()).valueAsMap();
		} else {
			return new HashMap<>();
		}
	}

	public static List<PerunAttribute> getFacilityAttributes(@NonNull AppBeansContainer applicationBeans,
															 @NonNull Long facilityId,
															 @NonNull AttributesProperties attributesProperties,
															 @NonNull InputsContainer inputsContainer,
															 @NonNull PerunAdapter perunAdapter)
			throws PerunUnknownException, PerunConnectionException
	{
		List<String> attrsToFetch = new ArrayList<>(applicationBeans.getAllAttrNames());
		Map<String, PerunAttribute> attrs = perunAdapter.getFacilityAttributes(facilityId, attrsToFetch);
		boolean isOidc = ServiceUtils.isOidcAttributes(attrs, attributesProperties.getNames().getOidcClientId());
		Set<String> keptAttrs = ServiceUtils.getAttrNames(inputsContainer, isOidc, attributesProperties);
		log.info("{}", keptAttrs);
		return ServiceUtils.filterFacilityAttrs(attrs, keptAttrs);
	}
}
