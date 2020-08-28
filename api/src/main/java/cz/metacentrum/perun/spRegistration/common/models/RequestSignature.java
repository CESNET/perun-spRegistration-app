package cz.metacentrum.perun.spRegistration.common.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Approval for request of transferring service into production environment.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class RequestSignature {

	@NotNull private Long requestId;
	@NotNull private Long userId;
	@NotNull private LocalDateTime signedAt;
	@NotBlank private String name;
	private boolean approved;

}
