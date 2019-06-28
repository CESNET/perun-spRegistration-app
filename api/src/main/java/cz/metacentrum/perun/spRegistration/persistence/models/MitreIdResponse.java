package cz.metacentrum.perun.spRegistration.persistence.models;

import java.util.Objects;

/**
 * Response from MitreID API. Holds interesting information from response that should be processed.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public class MitreIdResponse {

	private Long id;
	private String clientId;
	private String clientSecret;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	@Override
	public String toString() {
		return "MitreIdResponse{" +
				"id=" + id +
				", clientId='" + clientId + '\'' +
				", clientSecret='" + clientSecret + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MitreIdResponse that = (MitreIdResponse) o;
		return Objects.equals(id, that.id) &&
				Objects.equals(clientId, that.clientId) &&
				Objects.equals(clientSecret, that.clientSecret);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, clientId, clientSecret);
	}
}
