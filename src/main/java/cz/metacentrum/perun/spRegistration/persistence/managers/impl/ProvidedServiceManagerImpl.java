package cz.metacentrum.perun.spRegistration.persistence.managers.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.metacentrum.perun.spRegistration.Utils;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.models.ProvidedService;
import cz.metacentrum.perun.spRegistration.persistence.managers.ProvidedServiceManager;
import cz.metacentrum.perun.spRegistration.persistence.mappers.ProvidedServiceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;


@Component("providedServiceManager")
public class ProvidedServiceManagerImpl implements ProvidedServiceManager {

    private static final Logger log = LoggerFactory.getLogger(ProvidedServiceManagerImpl.class);
    private static final String SPS_TABLE = "provided_services";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ProvidedServiceMapper MAPPER = new ProvidedServiceMapper();

    @Autowired
    public ProvidedServiceManagerImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    @Transactional
    public ProvidedService create(ProvidedService sp) throws JsonProcessingException, InternalErrorException {
        log.trace("create({})", sp);

        if (Utils.checkParamsInvalid(sp)) {
            log.error("Wrong parameters passed: (sp : {})", sp);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        String query = new StringJoiner(" ")
                .add("INSERT INTO").add(SPS_TABLE)
                .add("(facility_id, name, description, environment, protocol, identifier)")
                .add("VALUES (:facility_id, :name, :description, :environment, :protocol, :identifier)")
                .toString();

        KeyHolder key = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("facility_id", sp.getFacilityId());
        params.addValue("name", sp.nameAsJsonString());
        params.addValue("description", sp.descriptionAsJsonString());
        params.addValue("environment", sp.getEnvironment().toString());
        params.addValue("protocol", sp.getProtocol().toString());
        params.addValue("identifier", sp.getIdentifier());

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
    @Transactional
    public boolean update(ProvidedService sp) throws JsonProcessingException {
        log.trace("update({})", sp);

        if (Utils.checkParamsInvalid(sp)) {
            log.error("Wrong parameters passed: (sp : {})", sp);
            throw new IllegalArgumentException(Utils.GENERIC_ERROR_MSG);
        }

        String query = new StringJoiner(" ")
                .add("UPDATE").add(SPS_TABLE)
                .add("SET facility_id = :facility_id, name = :name, description = :description, " +
                        "environment = :environment, protocol = :protocol, identifier = :identifier")
                .add("WHERE id = :id")
                .toString();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("facility_id", sp.getFacilityId());
        params.addValue("name", sp.nameAsJsonString());
        params.addValue("description", sp.descriptionAsJsonString());
        params.addValue("environment", sp.getEnvironment().toString());
        params.addValue("protocol", sp.getProtocol().toString());
        params.addValue("identifier", sp.getIdentifier());
        params.addValue("id", sp.getId());
        return this.executeUpdate(query, params);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
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
        return this.executeUpdate(query, params);
    }

    @Override
    @Transactional
    public boolean deleteByFacilityId(Long facilityId) {
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
        return this.executeUpdate(query, params);
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

        try {
            return jdbcTemplate.queryForObject(query, params, MAPPER);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
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

        try {
            return jdbcTemplate.queryForObject(query, params, MAPPER);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void recreate(ProvidedService sp) throws JsonProcessingException, InternalErrorException {
        String query = new StringJoiner(" ")
                .add("INSERT INTO").add(SPS_TABLE)
                .add("(id, facility_id, name, description, environment, protocol, identifier)")
                .add("VALUES (:id, :facility_id, :name, :description, :environment, :protocol, :identifier)")
                .toString();

        KeyHolder key = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", sp.getId());
        params.addValue("facility_id", sp.getFacilityId());
        params.addValue("name", sp.nameAsJsonString());
        params.addValue("description", sp.descriptionAsJsonString());
        params.addValue("environment", sp.getEnvironment().toString());
        params.addValue("protocol", sp.getProtocol().toString());
        params.addValue("identifier", sp.getIdentifier());

        int updatedCount = jdbcTemplate.update(query, params, key, new String[] { "id" });

        if (updatedCount == 0) {
            log.error("Zero sps have been inserted");
            throw new InternalErrorException("Zero sps have been inserted");
        } else if (updatedCount > 1) {
            log.error("Only one sp should have been inserted");
            throw new InternalErrorException("Only one sp should have been inserted");
        }
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
    public List<ProvidedService> getAllForFacilities(Set<Long> facilityIds) {
        return this.getAllForFacilities(new LinkedList<>(facilityIds));
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

        List<ProvidedService> providedServices = jdbcTemplate.query(query, params, MAPPER);

        log.trace("getAll returns: {}", providedServices);
        return providedServices;
    }

    private boolean executeUpdate(String query, MapSqlParameterSource params) {
        int updatedCount = jdbcTemplate.update(query, params);

        if (updatedCount == 0) {
            log.error("Zero sps have been deleted");
            throw new RuntimeException("Rollback needed");
        } else if (updatedCount > 1) {
            throw new RuntimeException("Rollback needed");
        } else {
            return true;
        }
    }
}
