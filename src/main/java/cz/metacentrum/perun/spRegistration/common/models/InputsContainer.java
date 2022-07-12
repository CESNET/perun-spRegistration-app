package cz.metacentrum.perun.spRegistration.common.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class InputsContainer {

    @NonNull private final List<AttrInput> serviceInputs;
    @NonNull private final List<AttrInput> organizationInputs;
    @NonNull private final List<AttrInput> membershipInputs;
    @NonNull private final List<AttrInput> oidcInputs;
    @NonNull private final List<AttrInput> samlInputs;

}
