package cz.metacentrum.perun.spRegistration.service.impl;

import cz.metacentrum.perun.spRegistration.Utils;
import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.exceptions.ExpiredCodeException;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.LinkCode;
import cz.metacentrum.perun.spRegistration.common.models.RequestDTO;
import cz.metacentrum.perun.spRegistration.common.models.RequestSignatureDTO;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.persistence.managers.LinkCodeManager;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestManager;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestSignatureManager;
import cz.metacentrum.perun.spRegistration.service.MailsService;
import cz.metacentrum.perun.spRegistration.service.RequestSignaturesService;
import cz.metacentrum.perun.spRegistration.service.UtilsService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static cz.metacentrum.perun.spRegistration.service.impl.MailsServiceImpl.REQUEST_SIGNED;

@Service("requestSignaturesService")
@Slf4j
public class RequestSignaturesServiceImpl implements RequestSignaturesService {

    private final RequestSignatureManager requestSignatureManager;
    private final RequestManager requestManager;
    private final MailsService mailsService;
    private final UtilsService utilsService;
    private final LinkCodeManager linkCodeManager;
    private final ApplicationProperties applicationProperties;

    @Autowired
    public RequestSignaturesServiceImpl(@NonNull RequestSignatureManager requestSignatureManager,
                                        @NonNull RequestManager requestManager,
                                        @NonNull MailsService mailsService,
                                        @NonNull UtilsService utilsService,
                                        @NonNull LinkCodeManager linkCodeManager,
                                        @NonNull ApplicationProperties applicationProperties)
    {
        this.requestSignatureManager = requestSignatureManager;
        this.requestManager = requestManager;
        this.mailsService = mailsService;
        this.applicationProperties = applicationProperties;
        this.utilsService = utilsService;
        this.linkCodeManager = linkCodeManager;
    }

    @Override
    public boolean addSignature(@NonNull User user, @NonNull String code, boolean approved)
            throws ExpiredCodeException, InternalErrorException
    {
        LinkCode linkCode = linkCodeManager.get(code);
        if (linkCode == null) {
            throw new ExpiredCodeException("Code has expired");
        } else if (linkCode.getRequestId() == null) {
            throw new InternalErrorException("Code has no request id");
        }

        Long requestId = linkCode.getRequestId();
        requestSignatureManager.addSignature(requestId, user, approved, code);
        RequestDTO req = requestManager.getRequestById(requestId);

        mailsService.notifyUser(req, REQUEST_SIGNED);
        mailsService.notifyAppAdmins(req, REQUEST_SIGNED);
        return true;
    }

    @Override
    public List<RequestSignatureDTO> getSignaturesForRequest(@NonNull Long requestId, @NonNull Long userId)
            throws UnauthorizedActionException, InternalErrorException, PerunUnknownException, PerunConnectionException {
        RequestDTO request = requestManager.getRequestById(requestId);
        if (request == null) {
            throw new InternalErrorException(Utils.GENERIC_ERROR_MSG);
        } else if (!applicationProperties.isAppAdmin(userId)
                && !utilsService.isAdminForRequest(request, userId)) {
            throw new UnauthorizedActionException(Utils.GENERIC_ERROR_MSG);
        }

        return requestSignatureManager.getRequestSignatures(requestId);
    }

}
