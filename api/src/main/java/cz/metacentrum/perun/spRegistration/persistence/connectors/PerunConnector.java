package cz.metacentrum.perun.spRegistration.persistence.connectors;

import cz.metacentrum.perun.spRegistration.persistence.exceptions.ConnectorException;
import cz.metacentrum.perun.spRegistration.persistence.models.Facility;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.persistence.models.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Connects to Perun and obtains information.
 *
 * @author Dominik Frantisek Bucik &lt;bucik@ics.muni.cz&gt;
 */
public interface PerunConnector {

	/**
	 * Create facility in Perun.
	 * @param facilityJson JSON of facility to be created.
	 * @return Created facility.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 */
	Facility createFacilityInPerun(JSONObject facilityJson) throws ConnectorException;

	/**
	 * Update existing facility in Perun.
	 * @param facilityJson JSON of facility to be created.
	 * @return Updated facility.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 */
	Facility updateFacilityInPerun(JSONObject facilityJson) throws ConnectorException;

	/**
	 * Delete facility from Perun.
	 * @param facilityId ID of facility to be deleted.
	 * @return True if everything went OK.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
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
	 * @throws IllegalArgumentException Thrown when param "proxyIdentifierAttr" is NULL or empty, when param "proxyIdentifier"
	 * is NULL or empty.
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
	 */
	Set<Long> getFacilityIdsWhereUserIsAdmin(Long userId) throws ConnectorException;

	/**
	 * Set attribute for facility in Perun.
	 * @param facilityId ID of facility.
	 * @param attrJson JSON representation of attribute.
	 * @return True if everything went OK.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 */
	boolean setFacilityAttribute(Long facilityId, JSONObject attrJson) throws ConnectorException;

	/**
	 * Set attributes for facility in Perun.
	 * @param facilityId ID of facility.
	 * @param attrsJsons List of JSON representations of attributes.
	 * @return True if everything went OK.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 */
	boolean setFacilityAttributes(Long facilityId, JSONArray attrsJsons) throws ConnectorException;

	/**
	 * Get user from Perun.
	 * @param extLogin sub from OIDC.
	 * @param extSourceName Perun extSource that has been used for login (entityId of Proxy)
	 * @param userEmailAttr user email.
	 * @return Retrieved user object.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 */
	User getUserWithEmail(String extLogin, String extSourceName, String userEmailAttr) throws ConnectorException;

	/**
	 * Add user as an admin (manager) of facility.
	 * @param facilityId ID of facility.
	 * @param userId ID of user.
	 * @return True if everything went OK.
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 */
	boolean addFacilityAdmin(Long facilityId, Long userId) throws ConnectorException;

	/**
	 * Fetch attribute definition by name.
	 * @param name Name of the attribute.
	 * @return Attribute definition
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
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
}