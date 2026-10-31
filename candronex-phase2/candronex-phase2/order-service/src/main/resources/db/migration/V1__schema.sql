CREATE TABLE orders (
    id          UUID        PRIMARY KEY,
    client_id   VARCHAR(64) NOT NULL,
    accepted_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_orders_client ON orders (client_id, accepted_at);

CREATE TABLE order_line (
    id                  UUID        PRIMARY KEY,
    order_id            UUID        NOT NULL REFERENCES orders (id),
    action              VARCHAR(16) NOT NULL,
    drone_id            VARCHAR(64) NOT NULL,
    service_offering_id VARCHAR(64) NOT NULL,
    CONSTRAINT uq_order_line UNIQUE (order_id, drone_id, service_offering_id)
);

-- Snapshot : caracteristiques de l'offre copiees au moment de la commande (sst, sd, dnn, 5qi...).
-- Le catalogue n'est jamais relu apres la commande.
CREATE TABLE order_line_characteristic (
    line_id UUID         NOT NULL REFERENCES order_line (id),
    name    VARCHAR(64)  NOT NULL,
    value   VARCHAR(200) NOT NULL,
    PRIMARY KEY (line_id, name)
);

-- Table d'idempotence : portee (client_id, idempotency_key), empreinte du contenu.
CREATE TABLE idempotency_key (
    client_id       VARCHAR(64)  NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    request_hash    VARCHAR(64)  NOT NULL,
    order_id        UUID         NOT NULL REFERENCES orders (id),
    created_at      TIMESTAMPTZ  NOT NULL,
    PRIMARY KEY (client_id, idempotency_key)
);
