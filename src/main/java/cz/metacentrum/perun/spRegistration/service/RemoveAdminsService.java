package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import lombok.NonNull;

public interface RemoveAdminsService {

    boolean removeAdmin(@NonNull User user, @NonNull Long facilityId, @NonNull Long AdminId)
            throws InternalErrorException, PerunUnknownException, PerunConnectionException;

}
