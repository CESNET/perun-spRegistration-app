package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.persistence.models.ProvidedService;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.util.List;

public interface FacilitiesService {

    /**
     * Get all facilities stored in Perun.
     * @param adminId ID of admin.
     * @return List of found facilities.
     * @throws UnauthorizedActionException when user is not authorized to perform this action.
     * @throws IllegalArgumentException Thrown when param "adminId" is NULL.
     */
    List<ProvidedService> getAllFacilities(Long adminId)
            throws UnauthorizedActionException, PerunUnknownException, PerunConnectionException;

    /**
     * Get all facilities from Perun where user is admin (manager).
     * @param userId ID of user.
     * @return List of facilities (empty or filled).
     * @throws IllegalArgumentException Thrown when param "userId" is NULL.
     */
    List<ProvidedService> getAllUserFacilities(Long userId) throws PerunUnknownException, PerunConnectionException;

    /**
     * Get detailed facility.
     * @param facilityId ID of facility.
     * @param userId ID of user.
     * @return Found facility.
     * @throws UnauthorizedActionException Thrown when user is not authorized to perform this action.
     * @throws IllegalArgumentException Thrown when param "facilityId" is NULL, when param "userId" is NULL.
     */
    Facility getFacility(Long facilityId, Long userId, boolean checkAdmin, boolean includeClientCredentials)
            throws UnauthorizedActionException, InternalErrorException, BadPaddingException, InvalidKeyException,
            IllegalBlockSizeException, PerunUnknownException, PerunConnectionException;

    /**
     * Get detailed facility.
     * @param facilityId ID of facility.
     * @param userId ID of user.
     * @return Found facility.
     * @throws UnauthorizedActionException when user is not authorized to perform this action.
     * @throws IllegalArgumentException Thrown when param "facilityId" is NULL, when param "userId" is NULL.
     */
    Facility getFacilityWithInputs(Long facilityId, Long userId)
            throws UnauthorizedActionException, InternalErrorException, BadPaddingException, InvalidKeyException,
            IllegalBlockSizeException, PerunUnknownException, PerunConnectionException;

}
