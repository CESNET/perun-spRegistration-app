package cz.metacentrum.perun.spRegistration.common.models;

import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;

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
public class RequestSignatureDTO {

	@NonNull private Long requestId;
	@NonNull private Long userId;
	@NonNull private LocalDateTime signedAt;
	@NonNull private String name;
	private boolean approved;

	public RequestSignatureDTO(Long requestId, Long userId, LocalDateTime signedAt, String name, boolean approved) {
		this.setRequestId(requestId);
		this.setUserId(userId);
		this.setSignedAt(signedAt);
		this.setName(name);
		this.setApproved(approved);
	}

	public void setName(String name) {
		if (!StringUtils.hasText(name)) {
			throw new IllegalArgumentException("Name cannot be empty");
		}

		this.name = name;
	}

}
