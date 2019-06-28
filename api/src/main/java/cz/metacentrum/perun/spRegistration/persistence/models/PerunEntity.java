package cz.metacentrum.perun.spRegistration.persistence.models;

/**
 * Abstract class representing entity in Perun
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
public abstract class PerunEntity {

	private Long id;

	public PerunEntity(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "PerunEntity{" +
				"id=" + id +
				'}';
	}
}
