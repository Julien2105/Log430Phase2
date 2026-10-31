package com.candronex.catalog.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "service_offering")
public class ServiceOfferingJpaEntity {

    @Id @Column(name = "id") private String id;
    @Column(name = "name") private String name;
    @Column(name = "description") private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "specification_id")
    private ServiceSpecificationJpaEntity specification;

    protected ServiceOfferingJpaEntity() { }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ServiceSpecificationJpaEntity getSpecification() { return specification; }
}
