package cz.metacentrum.perun.spRegistration.persistence.managers.impl;

import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.models.AuditLogDTO;
import cz.metacentrum.perun.spRegistration.persistence.managers.AuditLogsManager;
import cz.metacentrum.perun.spRegistration.persistence.mappers.AuditLogMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.StringJoiner;


@Component("auditLogsManager")
@Slf4j
public class AuditLogsManagerImpl implements AuditLogsManager {

    private static final String AUDIT_TABLE = "audit";
    private static final String PARAM_ID = "id";
    private static final String PARAM_REQUEST_ID = "request_id";
    private static final String PARAM_ACTOR_ID = "actor_id";
    private static final String PARAM_ACTOR_NAME = "actor_name";
    private static final String PARAM_MESSAGE = "message";
    private static final String PARAM_MESSAGE_TYPE = "message_type";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final AuditLogMapper MAPPER = new AuditLogMapper();

    @Autowired
    public AuditLogsManagerImpl(@NonNull NamedParameterJdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AuditLogDTO insert(@NonNull AuditLogDTO audit) throws InternalErrorException {
        String query = "INSERT INTO " + AUDIT_TABLE + "(" + AuditLogMapper.REQUEST_ID + ", " + AuditLogMapper.ACTOR_ID
                + ", " + AuditLogMapper.ACTOR_NAME + ", " + AuditLogMapper.MESSAGE_TYPE + ", "
                + AuditLogMapper.MESSAGE + ") " +
                "VALUES (:" + PARAM_REQUEST_ID + ", :" + PARAM_ACTOR_ID + ", :" + PARAM_ACTOR_NAME + ", :" + PARAM_MESSAGE + ")";

        KeyHolder key = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue(PARAM_REQUEST_ID, audit.getRequestId());
        params.addValue(PARAM_ACTOR_ID, audit.getActorId());
        params.addValue(PARAM_MESSAGE_TYPE, audit.getType().getValue());
        params.addValue(PARAM_ACTOR_NAME, audit.getActorName());
        params.addValue(PARAM_MESSAGE, audit.getMessage());

        int updatedCount = jdbcTemplate.update(query, params, key, new String[] { AuditLogMapper.ID });

        if (updatedCount == 0) {
            log.error("Zero audit entries have been inserted");
            throw new InternalErrorException("Zero audit entries have been inserted");
        } else if (updatedCount > 1) {
            log.error("Only one audit entry should have been inserted");
            throw new InternalErrorException("Only one audit entry should have been inserted");
        }

        Number generatedKey = key.getKey();
        if (generatedKey == null) {
            throw new InternalErrorException("Did not generate key");
        }

        Long id = generatedKey.longValue();
        audit.setId(id);
        return audit;
    }

    @Override
    public List<AuditLogDTO> getAll() {
        String query = new StringJoiner(" ")
                .add("SELECT * FROM").add(AUDIT_TABLE)
                .toString();
        return jdbcTemplate.query(query, MAPPER);
    }

    @Override
    public AuditLogDTO getById(@NonNull Long id) {
        String query = new StringJoiner(" ")
                .add("SELECT * FROM").add(AUDIT_TABLE)
                .add("WHERE").add(PARAM_ID).add("= :" + PARAM_ID)
                .toString();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue(PARAM_ID, id);
        try {
            return jdbcTemplate.queryForObject(query, params, MAPPER);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<AuditLogDTO> getForRequest(@NonNull Long requestId) {
        String query = new StringJoiner(" ")
                .add("SELECT * FROM").add(AUDIT_TABLE)
                .add("WHERE").add(PARAM_REQUEST_ID).add("= :" + PARAM_REQUEST_ID)
                .toString();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue(PARAM_REQUEST_ID, requestId);

        return jdbcTemplate.query(query, params, MAPPER);
    }

    @Override
    public List<AuditLogDTO> getForFacility(@NonNull Long facilityId) {
        String query = new StringJoiner(" ")
                .add("SELECT * FROM").add(AUDIT_TABLE)
                .add("WHERE").add(AuditLogMapper.REQUEST_ID).add("IN")
                .add("(SELECT id FROM requests WHERE facility_id = :" + PARAM_ID + ')')
                .toString();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue(PARAM_ID, facilityId);

        return jdbcTemplate.query(query, params, MAPPER);
    }

}
