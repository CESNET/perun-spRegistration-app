package cz.metacentrum.perun.spRegistration.persistence.mappers;


import cz.metacentrum.perun.spRegistration.common.enums.AuditMessageType;
import cz.metacentrum.perun.spRegistration.common.models.AuditLogDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AuditLogMapper implements RowMapper<AuditLogDTO> {

    public static final String ID = "id";
    public static final String REQUEST_ID = "request_id";
    public static final String ACTOR_ID = "actor_id";
    public static final String ACTOR_NAME = "actor_name";
    public static final String MESSAGE = "message";
    public static final String MESSAGE_TYPE = "message_type";
    public static final String MADE_AT = "made_at";

    @Override
    public AuditLogDTO mapRow(ResultSet resultSet, int i) throws SQLException {
        AuditLogDTO log = new AuditLogDTO();
        log.setId(resultSet.getLong(ID));
        log.setRequestId(resultSet.getLong(REQUEST_ID));
        log.setActorId(resultSet.getLong(ACTOR_ID));
        log.setActorName(resultSet.getString(ACTOR_NAME));
        log.setType(AuditMessageType.resolve(resultSet.getInt(MESSAGE_TYPE)));
        log.setMadeAt(resultSet.getTimestamp(MADE_AT));
        return log;
    }
}
