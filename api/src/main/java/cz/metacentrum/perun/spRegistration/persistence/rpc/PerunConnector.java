package cz.metacentrum.perun.spRegistration.persistence.rpc;

import cz.metacentrum.perun.spRegistration.persistence.exceptions.RPCException;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.models.Facility;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.persistence.models.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Connects to Perun and obtains information.
 *
 * @author Dominik František Bučík <bucik@ics.muni.cz>
 */
public interface PerunConnector {

	/**
	 * Create facility in Perun.
	 * @param facilityJson JSON of facility to be created.
	 * @return Created facility.
	 */
	Facility createFacilityInPerun(String facilityJson) throws RPCException;

	/**
	 * Update existing facility in Perun.
	 * @param facilityJson JSON of facility to be created.
	 * @return Updated facility.
	 */
	Facility updateFacilityInPerun(String facilityJson) throws RPCException;

	/**
	 * Delete facility from Perun.
	 * @param facilityId ID of facility to be deleted.
	 * @return True if everything went OK.
	 */
	boolean deleteFacilityFromPerun(Long facilityId) throws RPCException;

	/**
	 * Get facility from Perun with specified ID.
	 * @param facilityId ID of facility.
	 * @return Retrieved facility.
	 */
	Facility getFacilityById(Long facilityId) throws RPCException;

	/**
	 * Get facilities having specified attribute.
	 * @param attributesWithSearchingValues Map of attributes: key = attrName, value = attrValue.
	 * @return List of found facilities.
	 */
	List<Facility> getFacilitiesViaSearcher(Map<String, String> attributesWithSearchingValues) throws RPCException;

	/**
	 * Get facilities where user is admin (manager).
	 * @param userId ID of user.
	 * @return List of found facilities.
	 */
	List<Facility> getFacilitiesWhereUserIsAdmin(Long userId) throws RPCException;

	/**
	 * Get attribute of facility.
	 * @param facilityId ID of facility.
	 * @param attrName Name of the attribute.
	 * @return Retrieved attribute.
	 */
	PerunAttribute getFacilityAttribute(Long facilityId, String attrName) throws RPCException;

	/**
	 * Get all attributes of facility.
	 * @param facilityId ID of facility.
	 * @return Map (key = attribute name, value = attribute) of facility attributes.
	 */
	Map<String, PerunAttribute> getFacilityAttributes(Long facilityId) throws RPCException;

	/**
	 * Get specified attributes for facility.
	 * @param facilityId ID of facility.
	 * @param attrNames Names of attributes to be retrieved.
	 * @return Map (key = attribute name, value = attribute) of facility attributes.
	 */
	Map<String, PerunAttribute> getFacilityAttributes(Long facilityId, List<String> attrNames) throws RPCException;

	/**
	 * Get IDs of facilities where user is admin (manager).
	 * @param userId ID of user.
	 * @return Set of facility IDs.
	 */
	Set<Long> getFacilityIdsWhereUserIsAdmin(Long userId) throws RPCException;

	/**
	 * Set attribute for facility in Perun.
	 * @param facilityId ID of facility.
	 * @param attrJson JSON representation of attribute.
	 * @return True if everything went OK.
	 */
	boolean setFacilityAttribute(Long facilityId, String attrJson) throws RPCException;

	/**
	 * Set attributes for facility in Perun.
	 * @param facilityId ID of facility.
	 * @param attrsJsons List of JSON representations of attributes.
	 * @return True if everything went OK.
	 */
	boolean setFacilityAttributes(Long facilityId, List<String> attrsJsons) throws RPCException;

	/**
	 * Get user from Perun.
	 * @param extLogin sub from OIDC.
	 * @param extSourceName Perun extSource that has been used for login (entityId of Proxy)
	 * @param userEmailAttr user email.
	 * @return Retrieved user object.
	 */
	User getUserWithEmail(String extLogin, String extSourceName, String userEmailAttr) throws RPCException;

	/**
	 * Add user as an admin (manager) of facility.
	 * @param facilityId ID of facility.
	 * @param userId ID of user.
	 * @return True if everything went OK.
	 */
	boolean addFacilityAdmin(Long facilityId, Long userId) throws RPCException;

	/**
	 * Remove user from admins (managers) of facility.
	 * @param facilityId ID of facility.
	 * @param userId ID of user.
	 * @return True if everything went OK.
	 */
	boolean removeFacilityAdmin(Long facilityId, Long userId) throws RPCException;

	/**
	 * Fetch attribute definition by name.
	 * @param name Name of the attribute.
	 * @return Attribute definition
	 */
	PerunAttributeDefinition getAttributeDefinition(String name) throws RPCException;
}