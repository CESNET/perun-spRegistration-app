package cz.metacentrum.perun.spRegistration.persistence.managers.impl;

import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.models.LinkCode;
import cz.metacentrum.perun.spRegistration.persistence.managers.LinkCodeManager;
import cz.metacentrum.perun.spRegistration.persistence.mappers.LinkCodeMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Component("linkCodeManager")
@Slf4j
public class LinkCodeManagerImpl implements LinkCodeManager {

    public static final String PARAM_HASH = "hash";
    public static final String PARAM_RECIPIENT = "recipient";
    public static final String PARAM_SENDER_N = "sender_n";
    public static final String PARAM_SENDER_E = "sender_e";
    public static final String PARAM_EXP = "exp";
    public static final String PARAM_REQ_ID = "req_id";
    public static final String PARAM_FAC_ID = "fac_id";

    private static final String CODES_TABLE = "linkCodes";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final LinkCodeMapper MAPPER = new LinkCodeMapper();

    @Autowired
    public LinkCodeManagerImpl(@NonNull NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void create(LinkCode code) throws InternalErrorException {
       String query = new StringJoiner(" ")
                .add("INSERT INTO").add(CODES_TABLE)
                .add("(hash, recipient_email, sender_name, sender_email, expires_at, request_id, facility_id)")
                .add("VALUES (:hash, :recipient, :sender_n, :sender_e, :exp, :req_id, :fac_id)")
                .toString();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue(PARAM_HASH, code.getHash());
        parameters.addValue(PARAM_RECIPIENT, code.getRecipientEmail());
        parameters.addValue(PARAM_SENDER_N, code.getSenderName());
        parameters.addValue(PARAM_SENDER_E, code.getSenderEmail());
        parameters.addValue(PARAM_EXP, code.getExpiresAt().toInstant().toEpochMilli());
        parameters.addValue(PARAM_REQ_ID, code.getRequestId());
        parameters.addValue(PARAM_FAC_ID, code.getFacilityId());


        int insertedCodes = jdbcTemplate.update(query, parameters);

        if (insertedCodes != 1) {
            log.error("Failed insertion of code into the DB: {}, actually inserted: {}", code, insertedCodes);
            throw new InternalErrorException("Error in DB occurred");
        }
    }

    @Override
    @Transactional
    public void update(@NonNull LinkCode code) throws InternalErrorException {
        String query = new StringJoiner(" ")
                .add("UPDATE").add(CODES_TABLE)
                .add("SET recipient_email = :recipient, sender_name = :sender_n, sender_email = :sender_e,")
                .add("expires_at = :exp, request_id = :req_id, facility_id = :fac_id")
                .add("WHERE hash = :hash")
                .toString();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue(PARAM_RECIPIENT, code.getRecipientEmail());
        parameters.addValue(PARAM_SENDER_N, code.getSenderName());
        parameters.addValue(PARAM_SENDER_E, code.getSenderEmail());
        parameters.addValue(PARAM_EXP, code.getExpiresAt().toInstant().toEpochMilli());
        parameters.addValue(PARAM_REQ_ID, code.getRequestId());
        parameters.addValue(PARAM_FAC_ID, code.getFacilityId());
        parameters.addValue(PARAM_HASH, code.getHash());


        int updates = jdbcTemplate.update(query, parameters);

        if (updates != 1) {
            log.error("Failed updating code in the DB: {}, actually updated: {}", code, updates);
            throw new InternalErrorException("Error in DB occurred");
        }
    }

    @Override
    @Transactional
    public void delete(@NonNull String hash) throws InternalErrorException {
        String query = new StringJoiner(" ")
                .add("DELETE FROM").add(CODES_TABLE)
                .add("WHERE hash = :hash")
                .toString();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue(PARAM_HASH, hash);

        int removed = jdbcTemplate.update(query, parameters);

        if (removed != 1) {
            log.error("Failed removing code from the DB: {}, actually removed: {}", hash, removed);
            throw new InternalErrorException("Error in DB occurred");
        }
    }

    @Override
    @Transactional
    public void deleteExpired() {
        String query = new StringJoiner(" ")
                .add("DELETE FROM").add(CODES_TABLE)
                .add("WHERE expires_at <= :now")
                .toString();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("now", LocalDateTime.now());

        jdbcTemplate.update(query, parameters);
    }

    @Override
    public void createMultiple(@NonNull List<LinkCode> codes) throws InternalErrorException {
       String query = new StringJoiner(" ")
                .add("INSERT INTO").add(CODES_TABLE)
                .add("(hash, recipient_email, sender_name, sender_email, expires_at, request_id, facility_id)")
                .add("VALUES (:hash, :recipient, :sender_n, :sender_e, :exp, :req_id, :fac_id)")
                .toString();

        List<MapSqlParameterSource> batchArgs = new ArrayList<>();
        for (LinkCode code : codes) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue(PARAM_HASH, code.getHash());
            parameters.addValue(PARAM_RECIPIENT, code.getRecipientEmail());
            parameters.addValue(PARAM_SENDER_N, code.getSenderName());
            parameters.addValue(PARAM_SENDER_E, code.getSenderEmail());
            parameters.addValue(PARAM_EXP, code.getExpiresAt().toInstant().toEpochMilli());
            parameters.addValue(PARAM_REQ_ID, code.getRequestId());
            parameters.addValue(PARAM_FAC_ID, code.getFacilityId());
            batchArgs.add(parameters);
        }

        int[] created = jdbcTemplate.batchUpdate(query, batchArgs.toArray(
                new MapSqlParameterSource[codes.size()]));

        for (int i = 0; i < created.length; i++) {
            if (created[i] != 1) {
                log.error("Failed creating code in the DB: {}, actually created: {}", codes.get(i), created[i]);
                throw new InternalErrorException("Error in DB occurred");
            }
        }
    }

    @Override
    public LinkCode get(@NonNull String hash) {
        String query = new StringJoiner(" ")
                .add("SELECT * FROM").add(CODES_TABLE)
                .add("WHERE hash = :hash")
                .toString();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue(PARAM_HASH, hash);

        try {
            return jdbcTemplate.queryForObject(query, parameters, MAPPER);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public int deleteForRequest(Long reqId) {
        log.trace("deleteExpired()");

        String query = new StringJoiner(" ")
                .add("DELETE FROM").add(CODES_TABLE)
                .add("WHERE request_id = :req_id")
                .toString();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("req_id", reqId);

        int updates = jdbcTemplate.update(query, parameters);
        log.debug("removed {} link codes", updates);
        return updates;
    }

}
