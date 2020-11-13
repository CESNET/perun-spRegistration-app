package cz.metacentrum.perun.spRegistration.persistence.managers;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.models.ProvidedService;

import java.util.List;
import java.util.Set;

public interface ProvidedServiceManager {

    ProvidedService create(ProvidedService sp) throws JsonProcessingException, InternalErrorException;

    boolean update(ProvidedService sp) throws InternalErrorException, JsonProcessingException;

    boolean delete(Long id) throws InternalErrorException;

    boolean deleteByFacilityId(Long facilityId) throws InternalErrorException;

    ProvidedService get(Long id);

    List<ProvidedService> getAll();

    List<ProvidedService> getAllForFacilities(List<Long> facilityIds);

    List<ProvidedService> getAllForFacilities(Set<Long> facilityIds);

    ProvidedService getByFacilityId(Long facilityId);

    void recreate(ProvidedService sp) throws JsonProcessingException, InternalErrorException;
}
