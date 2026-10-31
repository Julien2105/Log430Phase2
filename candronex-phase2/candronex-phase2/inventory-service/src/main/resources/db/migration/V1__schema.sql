-- Seuls les services effectivement actives (ServiceInstance ACTIVE) figurent dans l'inventaire.
CREATE TABLE service_instance (
    service_id          UUID        PRIMARY KEY,
    client_id           VARCHAR(64) NOT NULL,
    order_id            UUID        NOT NULL,
    drone_id            VARCHAR(64) NOT NULL,
    service_offering_id VARCHAR(64) NOT NULL,
    state               VARCHAR(16) NOT NULL CHECK (state = 'ACTIVE'),
    activated_at        TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_instance_client_drone ON service_instance (client_id, drone_id);
CREATE INDEX idx_instance_order ON service_instance (order_id);
