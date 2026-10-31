package com.candronex.catalog.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "service_characteristic")
public class ServiceCharacteristicJpaEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "name") private String name;
    @Column(name = "value") private String value;

    protected ServiceCharacteristicJpaEntity() { }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getValue() { return value; }
}
