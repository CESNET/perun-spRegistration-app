package cz.metacentrum.perun.spRegistration.persistence.mappers;


import cz.metacentrum.perun.spRegistration.common.models.AuditLog;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AuditLogMapper implements RowMapper<AuditLog> {

    public static final String ID = "id";
    public static final String REQUEST_ID = "request_id";
    public static final String ACTOR_ID = "actor_id";
    public static final String ACTOR_NAME = "actor_name";
    public static final String MESSAGE = "message";
    public static final String MADE_AT = "made_at";

    @Override
    public AuditLog mapRow(ResultSet resultSet, int i) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(resultSet.getLong(ID));
        log.setRequestId(resultSet.getLong(REQUEST_ID));
        log.setActorId(resultSet.getLong(ACTOR_ID));
        log.setActorName(resultSet.getString(ACTOR_NAME));
        log.setMessage(resultSet.getString(MESSAGE));
        log.setMadeAt(resultSet.getTimestamp(MADE_AT));
        return log;
    }
}
