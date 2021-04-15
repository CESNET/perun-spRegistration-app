package cz.metacentrum.perun.spRegistration.rest.models;

import cz.metacentrum.perun.spRegistration.common.enums.RequestAction;
import cz.metacentrum.perun.spRegistration.common.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class RequestOverview {

    @NonNull private Long id;
    @NonNull private Map<String, String> serviceName;
    @NonNull private String serviceIdentifier;
    @NonNull private Long requesterId;
    private Long serviceId;
    @NonNull private RequestStatus status;
    @NonNull private RequestAction action;

}
