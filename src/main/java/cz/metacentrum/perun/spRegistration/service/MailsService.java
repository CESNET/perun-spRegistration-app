package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.ProvidedService;
import cz.metacentrum.perun.spRegistration.common.models.RequestDTO;
import cz.metacentrum.perun.spRegistration.common.models.User;
import java.util.Map;
import lombok.NonNull;

public interface MailsService {

    void notifyAuthorities(@NonNull RequestDTO req, @NonNull Map<String, String> authoritiesLinksMap);

    boolean notifyNewAdmins(@NonNull ProvidedService service, @NonNull Map<String, String> adminsLinksMap,
                            @NonNull User user);

    boolean authoritiesApproveProductionTransferNotify(@NonNull String approvalLink,
                                                       @NonNull RequestDTO req,
                                                       @NonNull String recipient);

    boolean adminAddRemoveNotify(@NonNull String approvalLink, @NonNull ProvidedService service,
                                 @NonNull String recipient, @NonNull User user);

    void notifyUser(@NonNull RequestDTO req, @NonNull String action);

    void notifyAppAdmins(@NonNull RequestDTO req, @NonNull String action);

    void notifyClientSecretChanged(@NonNull Facility facility);

}
