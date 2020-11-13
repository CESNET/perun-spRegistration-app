package cz.metacentrum.perun.spRegistration.common.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class LinkCode {

    private final String randomString = UUID.randomUUID().toString();

    private String hash;
    private String recipientEmail;
    private String senderName;
    private String senderEmail;
    private Timestamp expiresAt;
    private Long facilityId;
    private Long requestId;

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setExpiresAt(int plusDays, int plusHours) {
        this.expiresAt = new Timestamp(LocalDateTime.now()
                .plusDays(plusDays)
                .plusHours(plusHours)
                .toInstant(ZoneOffset.UTC).toEpochMilli());
    }

}
