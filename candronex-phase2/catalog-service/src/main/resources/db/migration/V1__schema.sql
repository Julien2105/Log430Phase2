-- Modèle du catalogue : ServiceOffering -> ServiceSpecification -> ServiceCharacteristic
CREATE TABLE service_specification (
    id   VARCHAR(64)  PRIMARY KEY,
    name VARCHAR(120) NOT NULL
);

CREATE TABLE service_offering (
    id               VARCHAR(64)  PRIMARY KEY,
    name             VARCHAR(120) NOT NULL,
    description      VARCHAR(400),
    specification_id VARCHAR(64)  NOT NULL REFERENCES service_specification (id)
);

CREATE TABLE service_characteristic (
    id               BIGSERIAL    PRIMARY KEY,
    specification_id VARCHAR(64)  NOT NULL REFERENCES service_specification (id),
    name             VARCHAR(64)  NOT NULL,
    value            VARCHAR(200) NOT NULL,
    CONSTRAINT uq_characteristic UNIQUE (specification_id, name)
);
