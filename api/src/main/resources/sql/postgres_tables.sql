create table requests
(
	id bigserial not null
		constraint requests_pkey
			primary key,
	facility_id bigint,
	status integer not null,
	action integer not null,
	requesting_user_id bigint not null,
	attributes varchar,
	modified_by bigint not null,
	modified_at timestamp default now()
);

create unique index requests_id_uindex on requests (id);

create table approvals
(
	facility_id bigint not null,
	link varchar,
	hash varchar,
	user_id bigint,
	user_email varchar,
	user_name varchar,
	valid_until timestamp,
	signed_at timestamp,
);