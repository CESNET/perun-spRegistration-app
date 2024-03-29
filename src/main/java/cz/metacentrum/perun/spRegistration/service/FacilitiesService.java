package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.ProvidedService;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import java.security.InvalidKeyException;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import lombok.NonNull;

public interface FacilitiesService {

    List<ProvidedService> getAllFacilities(@NonNull Long adminId)
        throws PerunUnknownException, PerunConnectionException;

    List<ProvidedService> getAllFacilitiesExternal(@NonNull Long userId)
        throws PerunUnknownException, PerunConnectionException;

    List<ProvidedService> getAllUserFacilities(@NonNull Long userId)
            throws PerunUnknownException, PerunConnectionException;

    List<ProvidedService> getAllUserFacilitiesExternal(@NonNull Long adminId)
        throws PerunUnknownException, PerunConnectionException;

    Facility getFacility(@NonNull Long facilityId, @NonNull Long userId, boolean includeClientCredentials)
            throws InternalErrorException, BadPaddingException, InvalidKeyException,
            IllegalBlockSizeException, PerunUnknownException, PerunConnectionException;

    Facility getFacilityWithInputs(@NonNull Long facilityId, @NonNull Long userId)
            throws InternalErrorException, BadPaddingException, InvalidKeyException,
            IllegalBlockSizeException, PerunUnknownException, PerunConnectionException;

    Facility getFacilityForSignature(@NonNull Long facilityId, @NonNull Long userId)
            throws BadPaddingException, PerunUnknownException, IllegalBlockSizeException, PerunConnectionException,
            InternalErrorException, InvalidKeyException;

}
