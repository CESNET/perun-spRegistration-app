package cz.metacentrum.perun.spRegistration.service.impl;

import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.common.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.common.models.Member;
import cz.metacentrum.perun.spRegistration.common.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.persistence.adapters.PerunAdapter;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunConnectionException;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.PerunUnknownException;
import cz.metacentrum.perun.spRegistration.service.RemoveAdminsService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service("removeAdminsService")
@Slf4j
public class RemoveAdminsServiceImpl implements RemoveAdminsService {

    @NonNull private final PerunAdapter perunAdapter;
    @NonNull private final AttributesProperties attributesProperties;
    @NonNull private final ApplicationProperties applicationProperties;

    @Autowired
    public RemoveAdminsServiceImpl(@NonNull PerunAdapter perunAdapter,
                                @NonNull AttributesProperties attributesProperties,
                                @NonNull ApplicationProperties applicationProperties)
    {
        this.perunAdapter = perunAdapter;
        this.attributesProperties = attributesProperties;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public boolean removeAdmins(@NonNull User user, @NonNull Long facilityId, @NonNull List<Long> adminsToRemoveIds)
            throws InternalErrorException, PerunUnknownException, PerunConnectionException
    {
        Long memberId = perunAdapter.getMemberIdByUser(applicationProperties.getSpManagersVoId(), user.getId());
        if (memberId == null) {
            throw new InternalErrorException();
        }

        PerunAttribute adminsGroupAttribute = perunAdapter.getFacilityAttribute(
                facilityId, attributesProperties.getNames().getManagerGroup());
        if (adminsGroupAttribute == null || adminsGroupAttribute.valueAsLong() == null) {
            throw new InternalErrorException();
        }

        List<Member> allGroupMembers = perunAdapter.getGroupMembers(adminsGroupAttribute.valueAsLong());
        List<Long> allAdminsIds = allGroupMembers.stream().map(Member::getId).collect(Collectors.toList());

        if (allAdminsIds.size() <= adminsToRemoveIds.size()) {
            return false;
        }

        return perunAdapter.removeMembersFromGroup(adminsGroupAttribute.valueAsLong(), adminsToRemoveIds);
    }

}
