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

CREATE TABLE IF NOT EXISTS signatureCodes
(
    code    TEXT
);