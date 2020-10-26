CREATE TABLE IF NOT EXISTS requests
(
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    facility_id        BIGINT,
    status             INTEGER NOT NULL,
    action             INTEGER NOT NULL,
    requesting_user_id BIGINT  NOT NULL,
    attributes         TEXT,
    modified_by        BIGINT  NOT NULL,
    modified_at        TIMESTAMP DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS requests_id_uindex ON requests (id);

CREATE TABLE IF NOT EXISTS approvals
(
    request_id BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    name       TEXT   NOT NULL,
    signed_at  TIMESTAMP DEFAULT now(),
    approved   BOOLEAN
);

CREATE TABLE IF NOT EXISTS linkCodes
(
    hash CHAR(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci PRIMARY KEY,
    recipient_email VARCHAR(512) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
    sender_name VARCHAR(512) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
    sender_email VARCHAR(512) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
    expires_at BIGINT NOT NULL,
    facility_id BIGINT,
    request_id BIGINT
);

CREATE TABLE IF NOT EXISTS provided_services
(
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    facility_id        BIGINT NOT NULL UNIQUE ,
    name               TINYTEXT NOT NULL,
    description        TEXT,
    environment        VARCHAR(256) NOT NULL,
    protocol           VARCHAR(256) NOT NULL,
    identifier         VARCHAR(4096) NOT NULL
);

CREATE TABLE IF NOT EXISTS audit
(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_id BIGINT NOT NULL,
    change_made_by BIGINT NOT NULL,
    change_description VARCHAR(256) NOT NULL,
    modified_at TIMESTAMP DEFAULT now()
);

CREATE TABLE IF NOT EXISTS service_to_request
(
    request_id BIGINT,
    service_id BIGINT,
    CONSTRAINT service_to_request_pk PRIMARY KEY (request_id, service_id)
);
