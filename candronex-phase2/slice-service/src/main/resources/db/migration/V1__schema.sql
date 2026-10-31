-- SliceService possede l'etat d'activation du Service et les dates acceptedAt / activatedAt (calcul du P95).
CREATE TABLE service (
    id                  UUID         PRIMARY KEY,           -- = identifiant de l'OrderLine (cle d'idempotence)
    order_id            UUID         NOT NULL,
    client_id           VARCHAR(64)  NOT NULL,
    drone_id            VARCHAR(64)  NOT NULL,
    imsi                VARCHAR(15)  NOT NULL,              -- interne : jamais expose
    service_offering_id VARCHAR(64)  NOT NULL,
    slice_type          VARCHAR(20),
    sst                 INTEGER,
    sd                  VARCHAR(6),
    dnn                 VARCHAR(120),
    five_qi             INTEGER,
    state               VARCHAR(16)  NOT NULL,
    accepted_at         TIMESTAMPTZ  NOT NULL,
    activated_at        TIMESTAMPTZ,
    failure_reason      VARCHAR(200),
    CONSTRAINT ck_service_state CHECK (state IN ('PENDING', 'ACTIVATING', 'ACTIVE', 'FAILED')),
    CONSTRAINT uq_service_order_line UNIQUE (order_id, drone_id, service_offering_id)
);
CREATE INDEX idx_service_state ON service (state);
CREATE INDEX idx_service_order ON service (order_id);
CREATE INDEX idx_service_accepted ON service (accepted_at);
CREATE INDEX idx_service_client_drone ON service (client_id, drone_id);
