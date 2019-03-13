package cz.metacentrum.perun.spRegistration.persistence.models;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Approval for request of transferring service into production environment.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class RequestSignature {

	private Long facilityId;
	private String hash;
	private String link;
	private Long signerId;
	private String signerName;
	private String signerEmail;
	private LocalDateTime signedAt;
	private LocalDateTime validUntil;

	public Long getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(Long facilityId) {
		this.facilityId = facilityId;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Long getSignerId() {
		return signerId;
	}

	public void setSignerId(Long signerId) {
		this.signerId = signerId;
	}

	public String getSignerName() {
		return signerName;
	}

	public void setSignerName(String signerName) {
		this.signerName = signerName;
	}

	public String getSignerEmail() {
		return signerEmail;
	}

	public void setSignerEmail(String signerEmail) {
		this.signerEmail = signerEmail;
	}

	public LocalDateTime getSignedAt() {
		return signedAt;
	}

	public void setSignedAt(LocalDateTime signedAt) {
		this.signedAt = signedAt;
	}

	public LocalDateTime getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(LocalDateTime validUntil) {
		this.validUntil = validUntil;
	}

	@Override
	public String toString() {
		return "RequestSignature{" +
				"facilityId=" + facilityId +
				", hash='" + hash + '\'' +
				", link='" + link + '\'' +
				", signerId=" + signerId +
				", signerName='" + signerName + '\'' +
				", signerEmail='" + signerEmail + '\'' +
				", signedAt=" + signedAt +
				", validUntil=" + validUntil +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RequestSignature them = (RequestSignature) o;
		return Objects.equals(this.facilityId, them.facilityId) &&
				Objects.equals(this.hash, them.hash) &&
				Objects.equals(this.link, them.link) &&
				Objects.equals(this.signerId, them.signerId) &&
				Objects.equals(this.signerName, them.signerName) &&
				Objects.equals(this.signerEmail, them.signerEmail) &&
				Objects.equals(this.validUntil, them.validUntil) &&
				Objects.equals(this.signedAt, them.signedAt);
	}

	@Override
	public int hashCode() {
		return Objects.hash(facilityId, hash, link, signerId, signerName, signerEmail, signedAt, validUntil);
	}
}
