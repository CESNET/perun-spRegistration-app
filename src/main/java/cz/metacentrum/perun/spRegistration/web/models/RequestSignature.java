package cz.metacentrum.perun.spRegistration.web.models;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

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
