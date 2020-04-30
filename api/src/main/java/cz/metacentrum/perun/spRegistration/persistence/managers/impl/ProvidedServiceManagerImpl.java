package cz.metacentrum.perun.spRegistration.persistence.managers.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.metacentrum.perun.spRegistration.Utils;
import cz.metacentrum.perun.spRegistration.persistence.managers.ProvidedServiceManager;
import cz.metacentrum.perun.spRegistration.persistence.mappers.ProvidedServiceMapper;
import cz.metacentrum.perun.spRegistration.persistence.models.ProvidedService;
import cz.metacentrum.perun.spRegistration.service.exceptions.InternalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.List;
import java.util.StringJoiner;

public class ProvidedServiceManagerImpl implements ProvidedServiceManager {

    private static final Logger log = LoggerFactory.getLogger(ProvidedServiceManagerImpl.class);
    private static final String SPS_TABLE = "provided_services";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ProvidedServiceMapper MAPPER = new ProvidedServiceMapper();

    public ProvidedServiceManagerImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public ProvidedService create(ProvidedService sp) throws JsonProcessingException, InternalErrorException {
        log.trace("create({})", sp);

        if (Utils.checkParamsInvalid(sp)) {
            log.error("Wrong parameters passed: (sp : {})", sp);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        String query = new StringJoiner(" ")
                .add("INSERT INTO").add(SPS_TABLE)
                .add("(facility_id, name, description, environment, protocol)")
                .add("VALUES (:facility_id, :name, :description, :environment, :protocol)")
                .toString();

        KeyHolder key = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("facility_id", sp.getFacilityId());
        params.addValue("name", sp.nameAsJsonString());
        params.addValue("description", sp.descriptionAsJsonString());
        params.addValue("environment", sp.getEnvironment());
        params.addValue("protocol", sp.getProtocol());

        int updatedCount = jdbcTemplate.update(query, params, key, new String[] { "id" });

        if (updatedCount == 0) {
            log.error("Zero sps have been inserted");
            throw new InternalErrorException("Zero sps have been inserted");
        } else if (updatedCount > 1) {
            log.error("Only one sp should have been inserted");
            throw new InternalErrorException("Only one sp should have been inserted");
        }

        Number generatedKey = key.getKey();
        Long generatedId = null;
        if (generatedKey != null) {
            generatedId = generatedKey.longValue();
        }
        sp.setId(generatedId);

        log.trace("create() returns: {}", sp);
        return sp;
    }

    @Override
    public void update(ProvidedService sp) throws InternalErrorException, JsonProcessingException {
        log.trace("update({})", sp);

        if (Utils.checkParamsInvalid(sp)) {
            log.error("Wrong parameters passed: (sp : {})", sp);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        String query = new StringJoiner(" ")
                .add("UPDATE").add(SPS_TABLE)
                .add("SET facility_id = :facility_id, name = :name, description = :description, environment = :environment, protocol = :protocol")
                .add("WHERE id = :id")
                .toString();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("facility_id", sp.getFacilityId());
        params.addValue("name", sp.nameAsJsonString());
        params.addValue("description", sp.descriptionAsJsonString());
        params.addValue("environment", sp.getEnvironment());
        params.addValue("protocol", sp.getProtocol());
        params.addValue("id", sp.getId());

        int updatedCount = jdbcTemplate.update(query, params);

        if (updatedCount == 0) {
            log.error("Zero sps have been updated");
            throw new InternalErrorException("Zero sps have been updated");
        } else if (updatedCount > 1) {
            log.error("Only one sp should have been updated");
            throw new InternalErrorException("Only one sp should have been updated");
        }
    }

    @Override
    public void delete(Long id) throws InternalErrorException {
        log.trace("delete({})", id);

        if (Utils.checkParamsInvalid(id)) {
            log.error("Wrong parameters passed: (id: {})", id);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        String query = new StringJoiner(" ")
                .add("DELETE FROM").add(SPS_TABLE)
                .add("WHERE id = :id")
                .toString();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        int updatedCount = jdbcTemplate.update(query, params);

        if (updatedCount == 0) {
            log.error("Zero sps have been deleted");
            throw new InternalErrorException("Zero sps have been deleted");
        } else if (updatedCount > 1) {
            log.error("Only one sp should have been deleted");
            throw new InternalErrorException("Only one sp should have been deleted");
        }
    }

    @Override
    public void deleteByFacilityId(Long facilityId) throws InternalErrorException {
        log.trace("deleteByFacilityId({})", facilityId);

        if (Utils.checkParamsInvalid(facilityId)) {
            log.error("Wrong parameters passed: (facilityId: {})", facilityId);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        String query = new StringJoiner(" ")
                .add("DELETE FROM").add(SPS_TABLE)
                .add("WHERE facility_id = :facility_id")
                .toString();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("facility_id", facilityId);

        int updatedCount = jdbcTemplate.update(query, params);

        if (updatedCount == 0) {
            log.error("Zero sps have been deleted");
            throw new InternalErrorException("Zero sps have been deleted");
        } else if (updatedCount > 1) {
            log.error("Only one sp should have been deleted");
            throw new InternalErrorException("Only one sp should have been deleted");
        }
    }

    @Override
    public ProvidedService get(Long id) {
        log.trace("get({})", id);

        if (Utils.checkParamsInvalid(id)) {
            log.error("Wrong parameters passed: (id: {})", id);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        String query = new StringJoiner(" ")
                .add("SELECT * FROM").add(SPS_TABLE)
                .add("WHERE id = :id")
                .toString();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        ProvidedService providedService = jdbcTemplate.queryForObject(query, params, MAPPER);

        log.trace("get({}) returns: {}", id, providedService);
        return providedService;
    }

    @Override
    public ProvidedService getByFacilityId(Long facilityId) {
        log.trace("getByFacilityId({})", facilityId);

        if (Utils.checkParamsInvalid(facilityId)) {
            log.error("Wrong parameters passed: (facilityId: {})", facilityId);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        String query = new StringJoiner(" ")
                .add("SELECT * FROM").add(SPS_TABLE)
                .add("WHERE facility_id = :facility_id")
                .toString();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("facility_id", facilityId);

        ProvidedService providedService = jdbcTemplate.queryForObject(query, params, MAPPER);

        log.trace("getByFacilityId({}) returns: {}", facilityId, providedService);
        return providedService;
    }

    @Override
    public List<ProvidedService> getAll() {
        log.trace("getAll()");

        String query = new StringJoiner(" ")
                .add("SELECT * FROM").add(SPS_TABLE)
                .toString();

        List<ProvidedService> providedServices = jdbcTemplate.query(query, MAPPER);

        log.trace("getAll returns: {}", providedServices);
        return providedServices;
    }

    @Override
    public List<ProvidedService> getAllForFacilities(List<Long> facilityIds) {
        log.trace("getAll()");
        if (facilityIds == null || facilityIds.isEmpty()) {
            return getAll();
        }

        MapSqlParameterSource params = new MapSqlParameterSource();

        StringJoiner sub = new StringJoiner(" OR ");
        for (Long id: facilityIds) {
            sub.add("facility_id = :facility_id" + id);
            params.addValue("facility_id" + id, id);
        }
        String query = new StringJoiner(" ")
                .add("SELECT * FROM").add(SPS_TABLE)
                .add("WHERE").add(sub.toString())
                .toString();

        List<ProvidedService> providedServices = jdbcTemplate.query(query, MAPPER);

        log.trace("getAll returns: {}", providedServices);
        return providedServices;
    }
}
