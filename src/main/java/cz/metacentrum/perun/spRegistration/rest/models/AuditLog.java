package cz.metacentrum.perun.spRegistration.rest.models;

import cz.metacentrum.perun.spRegistration.common.enums.AuditMessageType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = {"madeAt"})
public class AuditLog {

    @NonNull private Long requestId;
    @NonNull private Long actorId;
    @NonNull private String actorName;
    @NonNull private AuditMessageType type;
    @NonNull private Timestamp madeAt;

}
