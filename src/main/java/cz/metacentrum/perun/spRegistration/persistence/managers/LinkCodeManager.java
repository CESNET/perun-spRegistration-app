package cz.metacentrum.perun.spRegistration.persistence.managers;

import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.models.LinkCode;

import java.util.List;

public interface LinkCodeManager {

    void create(LinkCode code) throws InternalErrorException;

    void update(LinkCode code) throws InternalErrorException;

    void delete(String hash) throws InternalErrorException;

    void deleteExpired();

    void createMultiple(List<LinkCode> linkCodes) throws InternalErrorException;

    LinkCode get(String hash);
}
