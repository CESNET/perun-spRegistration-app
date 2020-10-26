package cz.metacentrum.perun.spRegistration.persistence.managers.impl;

import cz.metacentrum.perun.spRegistration.common.models.AuditLog;
import cz.metacentrum.perun.spRegistration.persistence.managers.AuditLogsManager;
import cz.metacentrum.perun.spRegistration.persistence.mappers.AuditLogMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.StringJoiner;


@Component("auditLogsManager")
@Slf4j
public class AuditLogsManagerImpl implements AuditLogsManager {

    private static final String AUDIT_TABLE = "audit";
    private static final String SERVICE_TO_REQUEST_TABLE = "service_to_request";
    private static final String PARAM_ID = "id";
    private static final String REQUEST_ID = "request_id";
    private static final String SERVICE_ID = "service_id";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final AuditLogMapper MAPPER = new AuditLogMapper();

    @Autowired
    public AuditLogsManagerImpl(@NonNull NamedParameterJdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<AuditLog> getAllAuditLogs() {
        String query = new StringJoiner(" ")
                .add("SELECT * FROM").add(AUDIT_TABLE)
                .toString();

        return jdbcTemplate.query(query, MAPPER);
    }

    @Override
    public AuditLog getAuditLogById(Long auditLogId) {
        String query = new StringJoiner(" ")
                .add("SELECT * FROM").add(AUDIT_TABLE)
                .add("WHERE id = :id")
                .toString();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue(PARAM_ID, auditLogId);

        return jdbcTemplate.queryForObject(query, params, MAPPER);
    }

    @Override
    public List<AuditLog> getAuditLogsByReqId(Long reqId) {
        String query = new StringJoiner(" ")
                .add("SELECT * FROM").add(AUDIT_TABLE)
                .add("WHERE request_id = :request_id")
                .toString();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue(REQUEST_ID, reqId);

        return jdbcTemplate.query(query, params, MAPPER);
    }

    @Override
    public List<AuditLog> getAuditLogsByService(Long serviceId) {
        String query = new StringJoiner(" ")
                .add("SELECT * FROM").add(AUDIT_TABLE)
                .add("NATURAL INNER JOIN").add(SERVICE_TO_REQUEST_TABLE)
                .add("WHERE service_id = :service_id")
                .toString();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue(SERVICE_ID, serviceId);

        return jdbcTemplate.query(query, params, MAPPER);
    }
}
