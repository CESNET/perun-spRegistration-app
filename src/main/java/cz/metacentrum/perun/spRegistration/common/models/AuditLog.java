package cz.metacentrum.perun.spRegistration.common.models;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NonNull;

import java.sql.Timestamp;

/**
 * Class represents audit log.
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>;
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = {"madeAt"})
public class AuditLog {

    @NonNull private Long id;
    @NonNull private Long requestId;
    @NonNull private Long actorId;
    @NonNull private String actorName;
    @NonNull private String message;
    private Timestamp madeAt;

}
