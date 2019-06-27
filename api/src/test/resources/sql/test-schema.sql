drop table if exists requests cascade;
drop table if exists approvals cascade;
drop table if exists signatureCodes cascade;

create table requests
(
    id                 bigint auto_increment
        constraint requests_pkey
        primary key,
    facility_id        bigint,
    status             integer not null,
    action             integer not null,
    requesting_user_id bigint  not null,
    attributes         varchar,
    modified_by        bigint  not null,
    modified_at        timestamp default now()
);

CREATE TABLE approvals
(
    request_id BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    name       TEXT   NOT NULL,
    signed_at  TIMESTAMP DEFAULT now(),
    approved   BOOLEAN
);

CREATE TABLE signatureCodes
(
    code    TEXT
);