package cz.metacentrum.perun.spRegistration.web.models;

import cz.metacentrum.perun.spRegistration.common.enums.AuditMessageType;
import java.sql.Timestamp;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

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
