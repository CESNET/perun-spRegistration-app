package cz.metacentrum.perun.spRegistration.persistence.managers.impl;

import cz.metacentrum.perun.spRegistration.Utils;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.models.LinkCode;
import cz.metacentrum.perun.spRegistration.persistence.managers.LinkCodeManager;
import cz.metacentrum.perun.spRegistration.persistence.mappers.LinkCodeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final String CODES_TABLE = "linkCodes";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final LinkCodeMapper MAPPER = new LinkCodeMapper();

    @Autowired
    public LinkCodeManagerImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void create(LinkCode code) throws InternalErrorException {
        log.trace("create({})", code);

        if (Utils.checkParamsInvalid(code)) {
            log.error("Wrong arguments passed: (code: {})", code);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        String query = new StringJoiner(" ")
                .add("INSERT INTO").add(CODES_TABLE)
                .add("(hash, recipient_email, sender_name, sender_email, expires_at, request_id, facility_id)")
                .add("VALUES (:hash, :recipient, :sender_n, :sender_e, :exp, :req_id, :fac_id)")
                .toString();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("hash", code.getHash());
        parameters.addValue("recipient", code.getRecipientEmail());
        parameters.addValue("sender_n", code.getSenderName());
        parameters.addValue("sender_e", code.getSenderEmail());
        parameters.addValue("exp", code.getExpiresAt().toInstant().toEpochMilli());
        parameters.addValue("req_id", code.getRequestId());
        parameters.addValue("fac_id", code.getFacilityId());


        int insertedCodes = jdbcTemplate.update(query, parameters);

        if (insertedCodes != 1) {
            log.error("Failed insertion of code into the DB: {}, actually inserted: {}", code, insertedCodes);
            throw new InternalErrorException("Error in DB occurred");
        }
    }

    @Override
    @Transactional
    public void update(LinkCode code) throws InternalErrorException {
        log.trace("update({})", code);

        if (Utils.checkParamsInvalid(code)) {
            log.error("Wrong arguments passed: (code: {})", code);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        String query = new StringJoiner(" ")
                .add("UPDATE").add(CODES_TABLE)
                .add("SET recipient_email = :recipient, sender_name = :sender_n, sender_email = :sender_e,")
                .add("expires_at = :exp, request_id = :req_id, facility_id = :fac_id")
                .add("WHERE hash = :hash1")
                .toString();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("hash", code.getHash());
        parameters.addValue("recipient", code.getRecipientEmail());
        parameters.addValue("sender_n", code.getSenderName());
        parameters.addValue("sender_e", code.getSenderEmail());
        parameters.addValue("exp", code.getExpiresAt().toInstant().toEpochMilli());
        parameters.addValue("req_id", code.getRequestId());
        parameters.addValue("fac_id", code.getFacilityId());


        int updates = jdbcTemplate.update(query, parameters);

        if (updates != 1) {
            log.error("Failed updating code in the DB: {}, actually updated: {}", code, updates);
            throw new InternalErrorException("Error in DB occurred");
        }
    }

    @Override
    @Transactional
    public void delete(String hash) throws InternalErrorException {
        log.trace("delete({})", hash);

        if (Utils.checkParamsInvalid(hash)) {
            log.error("Wrong arguments passed: (hash: {})", hash);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        String query = new StringJoiner(" ")
                .add("DELETE FROM").add(CODES_TABLE)
                .add("WHERE hash = :hash")
                .toString();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("hash", hash);

        int removed = jdbcTemplate.update(query, parameters);

        if (removed != 1) {
            log.error("Failed removing code from the DB: {}, actually removed: {}", hash, removed);
            throw new InternalErrorException("Error in DB occurred");
        }
    }

    @Override
    @Transactional
    public void deleteExpired() {
        log.trace("deleteExpired()");

        String query = new StringJoiner(" ")
                .add("DELETE FROM").add(CODES_TABLE)
                .add("WHERE expires_at <= :now")
                .toString();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("now", LocalDateTime.now());

        int updates = jdbcTemplate.update(query, parameters);
        log.debug("removed {} link codes", updates);
    }

    @Override
    public void createMultiple(List<LinkCode> codes) throws InternalErrorException {
        log.trace("createMultiple({})", codes);

        if (Utils.checkParamsInvalid(codes)) {
            log.error("Wrong arguments passed: (codes: {})", codes);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        String query = new StringJoiner(" ")
                .add("INSERT INTO").add(CODES_TABLE)
                .add("(hash, recipient_email, sender_name, sender_email, expires_at, request_id, facility_id)")
                .add("VALUES (:hash, :recipient, :sender_n, :sender_e, :exp, :req_id, :fac_id)")
                .toString();

        List<MapSqlParameterSource> batchArgs = new ArrayList<>();
        for (LinkCode code : codes) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("hash", code.getHash());
            parameters.addValue("recipient", code.getRecipientEmail());
            parameters.addValue("sender_n", code.getSenderName());
            parameters.addValue("sender_e", code.getSenderEmail());
            parameters.addValue("exp", code.getExpiresAt().toInstant().toEpochMilli());
            parameters.addValue("req_id", code.getRequestId());
            parameters.addValue("fac_id", code.getFacilityId());
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
    public LinkCode get(String hash) {
        log.trace("get({})", hash);

        String query = new StringJoiner(" ")
                .add("SELECT * FROM").add(CODES_TABLE)
                .add("WHERE hash = :hash")
                .toString();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("hash", hash);

        return jdbcTemplate.queryForObject(query, parameters, MAPPER);
    }
}
