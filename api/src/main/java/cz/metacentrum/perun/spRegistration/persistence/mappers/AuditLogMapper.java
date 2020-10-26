package cz.metacentrum.perun.spRegistration.persistence.mappers;


import cz.metacentrum.perun.spRegistration.common.models.AuditLog;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AuditLogMapper implements RowMapper<AuditLog> {

    private static final String ID = "id";
    private static final String REQUEST_ID = "request_id";
    private static final String CHANGE_MADE_BY = "change_made_by";
    private static final String CHANGE_DESCRIPTION = "change_description";
    private static final String MODIFIED_AT = "modified_at";

    @Override
    public AuditLog mapRow(@NotNull ResultSet resultSet, int i) throws SQLException {
        AuditLog log = new AuditLog();

        log.setId(resultSet.getLong(ID));
        log.setRequestId(resultSet.getLong(REQUEST_ID));
        log.setChangeMadeBy(resultSet.getLong(CHANGE_MADE_BY));
        log.setChangeDescription(resultSet.getString(CHANGE_DESCRIPTION));
        log.setModifiedAt(resultSet.getTimestamp(MODIFIED_AT));

        return log;
    }
}
