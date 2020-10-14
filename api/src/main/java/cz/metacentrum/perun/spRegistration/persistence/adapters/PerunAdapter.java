package cz.metacentrum.perun.spRegistration.persistence.adapters;

import com.fasterxml.jackson.databind.JsonNode;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.Group;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Connects to Perun and obtains information.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public interface PerunAdapter {

	Facility createFacilityInPerun(@NonNull String name, String description)
			throws PerunUnknownException, PerunConnectionException;

	/**
	 * Update existing facility in Perun.
	 * @param facilityJson JSON of facility to be created.
	 * @return Updated facility.
	 * @throws IllegalArgumentException Thrown when param "facilityJson" is NULL, equals JSONObject.NULL or empty.
	 */
	Facility updateFacilityInPerun(@NonNull JsonNode facilityJson)
			throws PerunUnknownException, PerunConnectionException;

	/**
	 * Delete facility from Perun.
	 * @param facilityId ID of facility to be deleted.
	 * @return True if everything went OK.
	 * @throws IllegalArgumentException Thrown when param "facilityId" is NULL.
	 */
	boolean deleteFacilityFromPerun(@NonNull Long facilityId)
			throws PerunUnknownException, PerunConnectionException;

	/**
	 * Get facility from Perun with specified ID.
	 * @param facilityId ID of facility.
	 * @return Retrieved facility.
	 * @throws IllegalArgumentException Thrown when param "facilityId" is NULL.
	 */
	Facility getFacilityById(@NonNull Long facilityId)
			throws PerunUnknownException, PerunConnectionException;

	/**
	 * Get facilities having specified attribute.
	 * @return List of found facilities.
	 * @throws IllegalArgumentException Thrown when param "proxyIdentifierAttr" is NULL or empty, when param
	 * "proxyIdentifier" is NULL or empty.
	 */
	List<Facility> getFacilitiesByProxyIdentifier(@NonNull String proxyIdentifierAttr, @NonNull String proxyIdentifier)
			throws PerunUnknownException, PerunConnectionException;

	/**
	 * Get facilities where user is admin (manager).
	 * @param userId ID of user.
	 * @return List of found facilities.
	 * @throws IllegalArgumentException Thrown when param "userId" is NULL.
	 */
	List<Facility> getFacilitiesWhereUserIsAdmin(@NonNull Long userId)
			throws PerunUnknownException, PerunConnectionException;

	/**
	 * Get attribute of facility.
	 * @param facilityId ID of facility.
	 * @param attrName Name of the attribute.
	 * @return Retrieved attribute.
	 * @throws IllegalArgumentException Thrown when param "facilityId" is NULL, when param "attrNames" is NULL.
	 */
	PerunAttribute getFacilityAttribute(@NonNull Long facilityId, @NonNull String attrName)
			throws PerunUnknownException, PerunConnectionException;

	/**
	 * Get specified attributes for facility.
	 * @param facilityId ID of facility.
	 * @param attrNames Names of attributes to be retrieved.
	 * @return Map (key = attribute name, value = attribute) of facility attributes.
	 * @throws IllegalArgumentException Thrown when param "facilityId" is NULL, when param "attrNames" is NULL.
	 */
	Map<String, PerunAttribute> getFacilityAttributes(@NonNull Long facilityId, @NonNull List<String> attrNames)
			throws PerunUnknownException, PerunConnectionException;

	/**
	 * Get IDs of facilities where user is admin (manager).
	 * @param userId ID of user.
	 * @return Set of facility IDs.
	 * @throws IllegalArgumentException Thrown when param "userId" is NULL.
	 */
	Set<Long> getFacilityIdsWhereUserIsAdmin(@NonNull Long userId)
			throws PerunUnknownException, PerunConnectionException;

	/**
	 * Set attribute for facility in Perun.
	 * @param facilityId ID of facility.
	 * @param attrJson JSON representation of attribute.
	 * @return True if everything went OK.
	 * @throws IllegalArgumentException Thrown when param "facilityId" is NULL, when param "attrJson" is NULL or empty
	 * or equals to JSONObject.NULL.
	 */
	boolean setFacilityAttribute(@NonNull Long facilityId, @NonNull JsonNode attrJson)
			throws PerunUnknownException, PerunConnectionException;

	/**
	 * Set attributes for facility in Perun.
	 * @param facilityId ID of facility.
	 * @param attrsJsons List of JSON representations of attributes.
	 * @return True if everything went OK.
	 * @throws IllegalArgumentException Thrown when param "facilityId" is NULL, when param "attrsJson" is NULL,
	 * empty or equals JSONObject.NULL.
	 */
	boolean setFacilityAttributes(@NonNull Long facilityId, @NonNull JsonNode attrsJsons)
			throws PerunUnknownException, PerunConnectionException;

	/**
	 * Get user from Perun.
	 * @param extLogin sub from OIDC.
	 * @param extSourceName Perun extSource that has been used for login (entityId of Proxy)
	 * @param userEmailAttr user email.
	 * @return Retrieved user object.
	 * @throws IllegalArgumentException Thrown when param "extLogin" is NULL or empty, when param "extSourceName"
	 * is NULL or empty, when param "userEmailAttr" is NULL or empty.
	 */
	User getUserWithEmail(@NonNull String extLogin, @NonNull String extSourceName, @NonNull String userEmailAttr)
			throws PerunUnknownException, PerunConnectionException;

	/**
	 * Fetch attribute definition by name.
	 * @param name Name of the attribute.
	 * @return Attribute definition
	 * @throws IllegalArgumentException Thrown when param "name" is NULL or empty.
	 */
	PerunAttributeDefinition getAttributeDefinition(@NonNull String name)
			throws PerunUnknownException, PerunConnectionException;

	/**
	 * Fetch facilities having specified attribute. Caller is fully responsible for "attrValue" param.
	 * This parameter is not checked in any way.
	 *
	 * @param attrName Name of the attribute.
	 * @param attrValue Value of the attribute.
	 * @return List of facilities, empty list if o such is found.
	 * @throws IllegalArgumentException Thrown when attrName is NULL or empty.
	 */
	List<Facility> getFacilitiesByAttribute(@NonNull String attrName, @NonNull String attrValue)
			throws PerunUnknownException, PerunConnectionException;

	User getUserById(@NonNull Long id)
			throws PerunUnknownException, PerunConnectionException;

	Group createGroup(Long parentGroupId, Group group) throws PerunUnknownException, PerunConnectionException;

	boolean deleteGroup(Long groupId) throws PerunUnknownException, PerunConnectionException;

	boolean addGroupAsAdmins(Long facilityId, Long groupId) throws PerunUnknownException, PerunConnectionException;

	boolean removeGroupFromAdmins(Long facilityId, Long groupId) throws PerunUnknownException, PerunConnectionException;

	Long getMemberIdByUser(Long vo, Long user) throws PerunUnknownException, PerunConnectionException;

	boolean addMemberToGroup(Long groupId, Long memberId) throws PerunUnknownException, PerunConnectionException;

	boolean removeMemberFromGroup(Long valueAsLong, Long memberId) throws PerunUnknownException, PerunConnectionException;
}