package cz.metacentrum.perun.spRegistration.common.models;


import cz.metacentrum.perun.spRegistration.common.enums.AuditMessageType;
import lombok.AccessLevel;
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
public class AuditLogDTO {

    @NonNull private Long id;
    @NonNull private Long requestId;
    @NonNull private Long actorId;
    @NonNull private String actorName;
    @NonNull private AuditMessageType type;
    @Setter(AccessLevel.PROTECTED)
    @NonNull private String message;
    private Timestamp madeAt;

    public void setType(@NonNull AuditMessageType type) {
        this.type = type;
        switch (type) {
            case REQUEST_REG_SERVICE_CREATED: this.message = "Service registration request created";
                break;
            case REQUEST_UPDATE_SERVICE_CREATED: this.message = "Update settings request created";
                break;
            case REQUEST_TRANSFER_SERVICE_CREATED: this.message = "Transfer to production request created";
                break;
            case REQUEST_REMOVE_SERVICE_CREATED: this.message = "Remove service request created";
                break;
            case REQUEST_APPROVED: this.message = "Approved by administrators";
                break;
            case REQUEST_REJECTED: this.message = "Rejected by administrators";
                break;
            case REQUEST_CHANGES_REQUEST: this.message = "Administrator requested changes in proposed settings";
                break;
            case REQUEST_UPDATED: this.message = "Updated by requester";
                break;
            case REQUEST_CANCELED: this.message = "Canceled by the requester";
                break;
        }
    }

}
