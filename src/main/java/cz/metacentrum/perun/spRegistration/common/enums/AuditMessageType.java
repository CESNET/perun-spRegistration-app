package cz.metacentrum.perun.spRegistration.common.enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum AuditMessageType {
    UNKNOWN(-1),
    REQUEST_REG_SERVICE_CREATED(0),
    REQUEST_UPDATE_SERVICE_CREATED(1),
    REQUEST_TRANSFER_SERVICE_CREATED(2),
    REQUEST_REMOVE_SERVICE_CREATED(3),
    REQUEST_APPROVED(4),
    REQUEST_REJECTED(5),
    REQUEST_CHANGES_REQUEST(6),
    REQUEST_UPDATED(7),
    REQUEST_CANCELED(8);

    private final int value;
    private static final Map<Integer, AuditMessageType> lookup = new HashMap<>();

    static {
        for (AuditMessageType status : EnumSet.allOf(AuditMessageType.class)) {
            lookup.put(status.value, status);
        }
    }

    AuditMessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AuditMessageType resolve(int code) {
        return lookup.get(code);
    }

}