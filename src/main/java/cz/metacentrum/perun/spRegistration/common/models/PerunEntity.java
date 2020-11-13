package cz.metacentrum.perun.spRegistration.common.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Abstract class representing entity in Perun
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public abstract class PerunEntity {

	private Long id;

}
