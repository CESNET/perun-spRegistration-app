package cz.metacentrum.perun.spRegistration.common.models;

import cz.metacentrum.perun.spRegistration.common.enums.MemberStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * Representation of Member.
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>;
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class Member extends PerunEntity {

    @NonNull private Long userId;
    @NonNull private Long voId;
    @NonNull private MemberStatus status;

    public Member(Long id) {
        super(id);
    }

    public Member(Long id, Long userId, Long voId, MemberStatus status) {
        super(id);
        this.setUserId(userId);
        this.setVoId(voId);
        this.setStatus(status);
    }

    public Member(Long id, Long userId, Long voId, String status) {
        this(id, userId, voId, MemberStatus.fromString(status));
    }

}
