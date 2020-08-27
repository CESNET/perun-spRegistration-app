package cz.metacentrum.perun.spRegistration.persistence.managers.impl;

import cz.metacentrum.perun.spRegistration.Utils;
import cz.metacentrum.perun.spRegistration.persistence.managers.LinkCodeManager;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestSignatureManager;
import cz.metacentrum.perun.spRegistration.persistence.mappers.RequestSignatureMapper;
import cz.metacentrum.perun.spRegistration.common.models.RequestSignature;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.StringJoiner;

@Component("requestSignatureManager")
@Slf4j
public class RequestSignatureManagerImpl implements RequestSignatureManager {

    private static final String APPROVALS_TABLE = "approvals";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final LinkCodeManager linkCodeManager;
    private final RequestSignatureMapper REQUEST_SIGNATURE_MAPPER;

    @Autowired
    public RequestSignatureManagerImpl(NamedParameterJdbcTemplate jdbcTemplate, LinkCodeManager linkCodeManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.linkCodeManager = linkCodeManager;
        REQUEST_SIGNATURE_MAPPER = new RequestSignatureMapper();
    }

    @Override
    @Transactional
    public boolean addSignature(Long requestId, Long userId, String userName, boolean approved, String code)
            throws InternalErrorException
    {
        log.trace("addSignature(requestId: {}, userId: {}, userName: {}, approved: {})",
                requestId, userId, userName, approved);

        if (Utils.checkParamsInvalid(requestId, userId, userId, code)) {
            log.error("Wrong parameters passed: (requestId: {}, user:Id {}, userName: {}, code: {})",
                    requestId, userId, userName, code);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        String query = new StringJoiner(" ")
                .add("INSERT INTO").add(APPROVALS_TABLE)
                .add("(request_id, user_id, name, approved) VALUES (:request_id, :user_id, :username, :approved)")
                .toString();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("request_id", requestId);
        params.addValue("user_id", userId);
        params.addValue("username", userName);
        params.addValue("approved", approved);

        int updatedCount = jdbcTemplate.update(query, params);

        if (updatedCount == 0) {
            log.error("Zero approvals have been inserted");
            throw new InternalErrorException(Utils.GENERIC_ERROR_MSG);
        } else if (updatedCount > 1) {
            log.error("Only one approval should have been inserted");
            throw new InternalErrorException(Utils.GENERIC_ERROR_MSG);
        }

        linkCodeManager.delete(code);

        log.trace("addSignature() returns: true");
        return true;
    }

    @Override
    @Transactional
    public List<RequestSignature> getRequestSignatures(Long requestId) {
        log.trace("getRequestSignatures({})", requestId);

        if (Utils.checkParamsInvalid(requestId)) {
            log.error("Illegal parameters passed: (requestId: {})", requestId);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        String query = new StringJoiner(" ")
                .add("SELECT * FROM").add(APPROVALS_TABLE)
                .add("WHERE request_id = :request_id")
                .toString();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("request_id", requestId);

        List<RequestSignature> foundApprovals = jdbcTemplate.query(query, params, REQUEST_SIGNATURE_MAPPER);

        log.trace("getRequestSignatures returns: {}", foundApprovals);

        return foundApprovals;
    }
}
