CREATE TABLE drone (
    client_id     VARCHAR(64)  NOT NULL,
    drone_id      VARCHAR(64)  NOT NULL,
    imsi          VARCHAR(15)  NOT NULL,
    label         VARCHAR(200),
    registered_at TIMESTAMPTZ  NOT NULL,
    -- droneId est unique PAR client : deux clients peuvent posseder DRN-0231
    CONSTRAINT pk_drone PRIMARY KEY (client_id, drone_id),
    CONSTRAINT uq_drone_imsi UNIQUE (imsi),
    CONSTRAINT ck_drone_imsi CHECK (imsi ~ '^[0-9]{15}$')
);
CREATE INDEX idx_drone_client ON drone (client_id);
