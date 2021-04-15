package cz.metacentrum.perun.spRegistration.rest.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class RequestSignature {

    @NonNull private Long signerId;
    @NonNull private String signerName;
    @NonNull private LocalDateTime signedAt;
    private boolean approved;

}
