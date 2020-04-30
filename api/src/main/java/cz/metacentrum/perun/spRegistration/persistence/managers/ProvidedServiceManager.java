package cz.metacentrum.perun.spRegistration.persistence.managers;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.persistence.models.ProvidedService;

import java.util.List;

public interface ProvidedServiceManager {

    ProvidedService create(ProvidedService sp) throws JsonProcessingException, InternalErrorException;

    void update(ProvidedService sp) throws InternalErrorException, JsonProcessingException;

    void delete(Long id) throws InternalErrorException;

    void deleteByFacilityId(Long facilityId) throws InternalErrorException;

    ProvidedService get(Long id);

    List<ProvidedService> getAll();

    List<ProvidedService> getAllForFacilities(List<Long> facilityIds);

    ProvidedService getByFacilityId(Long facilityId);
}
