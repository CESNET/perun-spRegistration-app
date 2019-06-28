package cz.metacentrum.perun.spRegistration.persistence.connectors;

import cz.metacentrum.perun.spRegistration.persistence.exceptions.ConnectorException;
import cz.metacentrum.perun.spRegistration.persistence.models.MitreIdResponse;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttribute;

import java.util.Map;

/**
 * Connects to MitreID API
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public interface MitreIdConnector {

	/**
	 * Create client in mitreid
	 * @param attrs Attributes to be set
	 * @return Response from mitreId containing some interesting fields
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 */
	MitreIdResponse createClient(Map<String, PerunAttribute> attrs) throws ConnectorException;

	/**
	 * Update client in mitreid
	 * @param id id of client in mitreid (not client_id)
	 * @param attrs attributes to be set
	 * @return True if update was successful, false otherwise
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 */
	boolean updateClient(Long id, Map<String, PerunAttribute> attrs) throws ConnectorException;

	/**
	 * Delete client from mitreid
	 * @param id id of client in mitreid (not client_id)
	 * @return True if delete was successful, false otherwise
	 * @throws ConnectorException Thrown when problem while communicating with Perun RPC occur.
	 */
	boolean deleteClient(Long id) throws ConnectorException;
}
