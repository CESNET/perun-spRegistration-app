package cz.metacentrum.perun.spRegistration.persistence.connectors.impl;

import cz.metacentrum.perun.spRegistration.persistence.configs.MitreIdAttrsConfig;
import cz.metacentrum.perun.spRegistration.persistence.connectors.ConnectorUtils;
import cz.metacentrum.perun.spRegistration.persistence.connectors.MitreIdConnector;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.ConnectorException;
import cz.metacentrum.perun.spRegistration.persistence.models.MitreIdResponse;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttribute;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Implementation of connector to MITREid server.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class MitreIdConnectorImpl implements MitreIdConnector {

	private static final Logger log = LoggerFactory.getLogger(MitreIdConnectorImpl.class);
	private static final String ID = "id";
	private static final String CLIENT_ID = "clientId";
	private static final String CLIENT_SECRET = "clientSecret";
	private static final String CLIENT_NAME = "clientName";
	private static final String CLIENT_DESCRIPTION = "clientDescription";
	private static final String SCOPE = "scope";
	private static final String REDIRECT_URIS = "redirectUris";
	private static final String CLIENT_URI = "clientUri";
	private static final String GRANT_TYPES = "grantTypes";
	private static final String RESPONSE_TYPES = "responseTypes";
	private static final String POLICY_URI = "policyUri";
	private static final String ALLOW_INTROSPECTION = "allowIntrospection";
	private static final String CONTACTS = "contacts";

	private final MitreIdAttrsConfig attrsConfig;

	private final String username;
	private final String password;
	private final String clientsEndpoint;

	public MitreIdConnectorImpl(MitreIdAttrsConfig attrsConfig, String username, String password,
								String clientsEndpoint) {
		this.attrsConfig = attrsConfig;
		this.username = username;
		this.password = password;
		this.clientsEndpoint = clientsEndpoint;
	}

	@Override
	public MitreIdResponse createClient(Map<String, PerunAttribute> attrs) throws ConnectorException {
		JSONObject body = updateJsonObject(null, attrs);

		JSONObject response = makeCall(clientsEndpoint, body, RequestMethod.POST);
		Long id = response.getLong(ID);
		String clientId = response.getString(CLIENT_ID);
		String clientSecret = response.getString(CLIENT_SECRET);

		MitreIdResponse resp = new MitreIdResponse();
		resp.setClientId(clientId);
		resp.setId(id);
		resp.setClientSecret(clientSecret);

		return resp;
	}

	@Override
	public boolean updateClient(Long id, Map<String, PerunAttribute> attrs) throws ConnectorException {
		JSONObject actualClient = makeCall(clientsEndpoint + "/" + id, null, RequestMethod.GET);
		JSONObject newClient = updateJsonObject(actualClient, attrs);
		JSONObject response = makeCall(clientsEndpoint, newClient, RequestMethod.PUT);

		return response == null;
	}

	@Override
	public boolean deleteClient(Long id) throws ConnectorException {
		JSONObject response = makeCall(clientsEndpoint + "/" + id, null, RequestMethod.DELETE);
		return response == null;
	}

	private JSONObject updateJsonObject(JSONObject obj, Map<String, PerunAttribute> attrs) {
		if (obj == null) {
			obj = new JSONObject();
		}

		obj.put(CLIENT_NAME, attrs.get(attrsConfig.getClientNameAttr()).valueAsString());
		obj.put(CLIENT_DESCRIPTION, attrs.get(attrsConfig.getClientDescriptionAttr()).valueAsString());
		obj.put(SCOPE, attrs.get(attrsConfig.getRequiredScopesAttr()).valueAsArray());
		obj.put(REDIRECT_URIS, attrs.get(attrsConfig.getRedirectUrisAttr()).valueAsArray());
		obj.put(CLIENT_URI, attrs.get(attrsConfig.getInformationUrlAttr()).valueAsString());
		obj.put(GRANT_TYPES, attrs.get(attrsConfig.getGrantTypesAttrs()).valueAsArray());
		obj.put(RESPONSE_TYPES, attrs.get(attrsConfig.getResponseTypesAttr()).valueAsArray());
		obj.put(POLICY_URI, attrs.get(attrsConfig.getPrivacyPolicyUriAttr()).valueAsString());
		obj.put(ALLOW_INTROSPECTION, attrs.get(attrsConfig.getAllowIntrospectionAttr()).valueAsBoolean());
		obj.put(CONTACTS, Collections.singletonList(attrs.get(attrsConfig.getContactAttr()).valueAsString()));

		return obj;
	}

	private JSONObject makeCall(String endpoint, JSONObject body, RequestMethod method) throws ConnectorException {
		log.trace("makeCallForObject(endpoint: {}, body: {}", endpoint, body);

		RestTemplate restTemplate = new RestTemplate();
		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
		interceptors.add(new BasicAuthorizationInterceptor(username, password));
		restTemplate.setRequestFactory(new InterceptingClientHttpRequestFactory(restTemplate.getRequestFactory(), interceptors));

		//make the call
		try {
			JSONObject response = null;
			switch (method) {
				case POST: {
					response = restTemplate.postForObject(endpoint, body, JSONObject.class);
				} break;
				case PUT: {
					restTemplate.put(endpoint, body, JSONObject.class);
				} break;
				case GET: {
					response = restTemplate.getForEntity(endpoint, JSONObject.class).getBody();
				} break;
				case DELETE: {
					restTemplate.delete(endpoint);
				} break;
				default:
					throw new ConnectorException("Unsupported method: " + method);
			}

			log.trace("makeCall returns: {}", response);
			return response;
		} catch (HttpClientErrorException ex) {
			return new JSONObject(ConnectorUtils.dealWithHttpClientErrorException(ex, "Could not connect to mitreid"));
		}
	}
}
