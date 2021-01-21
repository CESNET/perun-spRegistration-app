package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import lombok.NonNull;

import java.util.List;

public interface RemoveAdminsService {

    boolean removeAdmins(@NonNull User user, @NonNull Long facilityId, @NonNull List<Long> allAdminsIds)
            throws InternalErrorException, PerunUnknownException, PerunConnectionException;

}
