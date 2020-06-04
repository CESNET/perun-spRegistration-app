package cz.metacentrum.perun.spRegistration.persistence.connectors;

import com.fasterxml.jackson.databind.JsonNode;
import cz.metacentrum.perun.spRegistration.common.exceptions.BadRequestException;
import cz.metacentrum.perun.spRegistration.common.exceptions.ConnectorException;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.Group;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.common.models.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Connects to Perun and obtains information.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public interface PerunConnector {

	/**
	 * Create facility in Perun.
	 * @param facilityJson JSON of facility to be created.
	 * @return Created facility.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws IllegalArgumentException Thrown when param "facilityJson" is NULL, equals JSONObject.NULL or empty.
	 */
	Facility createFacilityInPerun(JsonNode facilityJson) throws ConnectorException, BadRequestException;

	/**
	 * Update existing facility in Perun.
	 * @param facilityJson JSON of facility to be created.
	 * @return Updated facility.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws IllegalArgumentException Thrown when param "facilityJson" is NULL, equals JSONObject.NULL or empty.
	 */
	Facility updateFacilityInPerun(JsonNode facilityJson) throws ConnectorException, BadRequestException;

	/**
	 * Delete facility from Perun.
	 * @param facilityId ID of facility to be deleted.
	 * @return True if everything went OK.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws IllegalArgumentException Thrown when param "facilityId" is NULL.
	 */
	boolean deleteFacilityFromPerun(Long facilityId) throws ConnectorException;

	/**
	 * Get facility from Perun with specified ID.
	 * @param facilityId ID of facility.
	 * @return Retrieved facility.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws IllegalArgumentException Thrown when param "facilityId" is NULL.
	 */
	Facility getFacilityById(Long facilityId) throws ConnectorException;

	/**
	 * Get facilities having specified attribute.
	 * @return List of found facilities.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws IllegalArgumentException Thrown when param "proxyIdentifierAttr" is NULL or empty, when param
	 * "proxyIdentifier" is NULL or empty.
	 */
	List<Facility> getFacilitiesByProxyIdentifier(String proxyIdentifierAttr, String proxyIdentifier)
			throws ConnectorException;

	/**
	 * Get facilities where user is admin (manager).
	 * @param userId ID of user.
	 * @return List of found facilities.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws IllegalArgumentException Thrown when param "userId" is NULL.
	 */
	List<Facility> getFacilitiesWhereUserIsAdmin(Long userId) throws ConnectorException;

	/**
	 * Get attribute of facility.
	 * @param facilityId ID of facility.
	 * @param attrName Name of the attribute.
	 * @return Retrieved attribute.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws IllegalArgumentException Thrown when param "facilityId" is NULL, when param "attrNames" is NULL.
	 */
	PerunAttribute getFacilityAttribute(Long facilityId, String attrName) throws ConnectorException;

	/**
	 * Get specified attributes for facility.
	 * @param facilityId ID of facility.
	 * @param attrNames Names of attributes to be retrieved.
	 * @return Map (key = attribute name, value = attribute) of facility attributes.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws IllegalArgumentException Thrown when param "facilityId" is NULL, when param "attrNames" is NULL.
	 */
	Map<String, PerunAttribute> getFacilityAttributes(Long facilityId, List<String> attrNames) throws ConnectorException;

	/**
	 * Get IDs of facilities where user is admin (manager).
	 * @param userId ID of user.
	 * @return Set of facility IDs.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws IllegalArgumentException Thrown when param "userId" is NULL.
	 */
	Set<Long> getFacilityIdsWhereUserIsAdmin(Long userId) throws ConnectorException;

	/**
	 * Set attribute for facility in Perun.
	 * @param facilityId ID of facility.
	 * @param attrJson JSON representation of attribute.
	 * @return True if everything went OK.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws IllegalArgumentException Thrown when param "facilityId" is NULL, when param "attrJson" is NULL or empty
	 * or equals to JSONObject.NULL.
	 */
	boolean setFacilityAttribute(Long facilityId, JsonNode attrJson) throws ConnectorException;

	/**
	 * Set attributes for facility in Perun.
	 * @param facilityId ID of facility.
	 * @param attrsJsons List of JSON representations of attributes.
	 * @return True if everything went OK.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws IllegalArgumentException Thrown when param "facilityId" is NULL, when param "attrsJson" is NULL,
	 * empty or equals JSONObject.NULL.
	 */
	boolean setFacilityAttributes(Long facilityId, JsonNode attrsJsons) throws ConnectorException;

	/**
	 * Get user from Perun.
	 * @param extLogin sub from OIDC.
	 * @param extSourceName Perun extSource that has been used for login (entityId of Proxy)
	 * @param userEmailAttr user email.
	 * @return Retrieved user object.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws IllegalArgumentException Thrown when param "extLogin" is NULL or empty, when param "extSourceName"
	 * is NULL or empty, when param "userEmailAttr" is NULL or empty.
	 */
	User getUserWithEmail(String extLogin, String extSourceName, String userEmailAttr) throws ConnectorException;

	/**
	 * Fetch attribute definition by name.
	 * @param name Name of the attribute.
	 * @return Attribute definition
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws IllegalArgumentException Thrown when param "name" is NULL or empty.
	 */
	PerunAttributeDefinition getAttributeDefinition(String name) throws ConnectorException;

	/**
	 * Fetch facilities having specified attribute. Caller is fully responsible for "attrValue" param.
	 * This parameter is not checked in any way.
	 *
	 * @param attrName Name of the attribute.
	 * @param attrValue Value of the attribute.
	 * @return List of facilities, empty list if o such is found.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 * @throws IllegalArgumentException Thrown when attrName is NULL or empty.
	 */
	List<Facility> getFacilitiesByAttribute(String attrName, String attrValue) throws ConnectorException;

	User getUserById(Long id) throws ConnectorException;


	Group createGroup(Long parentGroupId, Group group) throws ConnectorException;

	boolean deleteGroup(Long groupId) throws ConnectorException;

	boolean addGroupAsAdmins(Long facilityId, Long groupId) throws ConnectorException;

	boolean removeGroupFromAdmins(Long facilityId, Long groupId) throws ConnectorException;

	Long getMemberIdByUser(Long vo, Long user) throws ConnectorException;

	boolean addMemberToGroup(Long groupId, Long memberId) throws ConnectorException;
}