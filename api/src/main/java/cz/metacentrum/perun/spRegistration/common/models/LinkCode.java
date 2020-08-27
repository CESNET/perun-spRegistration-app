package cz.metacentrum.perun.spRegistration.common.models;

import cz.metacentrum.perun.spRegistration.common.configs.ApprovalsProperties;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

public class LinkCode {

    private String hash;
    private String recipientEmail;
    private String senderName;
    private String senderEmail;
    private Timestamp expiresAt;
    private Long facilityId;
    private Long requestId;
    private final String randomString = UUID.randomUUID().toString();

    public LinkCode() { }

    public LinkCode(String hash, String recipientEmail, String senderName, String senderEmail, Timestamp expiresAt,
                    Long facilityId, Long requestId) {
        this.hash = hash;
        this.recipientEmail = recipientEmail;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.expiresAt = expiresAt;
        this.facilityId = facilityId;
        this.requestId = requestId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setExpiresAt(ApprovalsProperties approvalsProperties) {
        this.expiresAt = new Timestamp(LocalDateTime.now()
                .plusHours(approvalsProperties.getConfirmationPeriodHours())
                .plusDays(approvalsProperties.getConfirmationPeriodDays())
                .toInstant(ZoneOffset.UTC).toEpochMilli());
    }

    public Long getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Long facilityId) {
        this.facilityId = facilityId;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "LinkCode.ts{" +
                "hash='" + hash + '\'' +
                ", recipientEmail='" + recipientEmail + '\'' +
                ", senderName='" + senderName + '\'' +
                ", senderEmail='" + senderEmail + '\'' +
                ", expiresAt=" + expiresAt +
                ", facilityId=" + facilityId +
                ", requestId=" + requestId +
                ", randomString='" + randomString + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinkCode linkCode = (LinkCode) o;
        return Objects.equals(hash, linkCode.hash) &&
                Objects.equals(recipientEmail, linkCode.recipientEmail) &&
                Objects.equals(senderName, linkCode.senderName) &&
                Objects.equals(senderEmail, linkCode.senderEmail) &&
                Objects.equals(expiresAt, linkCode.expiresAt) &&
                Objects.equals(facilityId, linkCode.facilityId) &&
                Objects.equals(requestId, linkCode.requestId) &&
                Objects.equals(randomString, linkCode.randomString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, recipientEmail, senderName, senderEmail, expiresAt, facilityId, requestId,
                randomString);
    }
}
