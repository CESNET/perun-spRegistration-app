package cz.metacentrum.perun.spRegistration.persistence.managers.impl;

import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.models.RequestSignature;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.managers.LinkCodeManager;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestSignatureManager;
import cz.metacentrum.perun.spRegistration.persistence.mappers.RequestSignatureMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
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

    public static final String PARAM_REQUEST_ID = "request_id";
    public static final String PARAM_USER_ID = "user_id";
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_APPROVED = "approved";

    private static final String APPROVALS_TABLE = "approvals";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final LinkCodeManager linkCodeManager;
    private final RequestSignatureMapper requestSignatureMapper;

    @Autowired
    public RequestSignatureManagerImpl(@NonNull NamedParameterJdbcTemplate jdbcTemplate,
                                       @NonNull LinkCodeManager linkCodeManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.linkCodeManager = linkCodeManager;
        requestSignatureMapper = new RequestSignatureMapper();
    }

    @Override
    @Transactional
    public void addSignature(@NonNull Long requestId, @NonNull User signer, boolean approved, @NonNull String code)
            throws InternalErrorException
    {
        String query = new StringJoiner(" ")
                .add("INSERT INTO").add(APPROVALS_TABLE)
                .add("(request_id, user_id, name, approved) VALUES (:request_id, :user_id, :username, :approved)")
                .toString();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue(PARAM_REQUEST_ID, requestId);
        params.addValue(PARAM_USER_ID, signer.getId());
        params.addValue(PARAM_USERNAME, signer.getName());
        params.addValue(PARAM_APPROVED, approved);

        int updatedCount = jdbcTemplate.update(query, params);

        if (updatedCount == 0) {
            log.error("Zero approvals have been inserted");
            throw new InternalErrorException();
        } else if (updatedCount > 1) {
            log.error("Only one approval should have been inserted");
            throw new InternalErrorException();
        }

        linkCodeManager.delete(code);
    }

    @Override
    @Transactional
    public List<RequestSignature> getRequestSignatures(@NonNull Long requestId) {
       String query = new StringJoiner(" ")
                .add("SELECT * FROM").add(APPROVALS_TABLE)
                .add("WHERE request_id = :request_id")
                .toString();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue(PARAM_REQUEST_ID, requestId);

        return jdbcTemplate.query(query, params, requestSignatureMapper);
    }

}
