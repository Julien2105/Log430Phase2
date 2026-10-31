package com.candronex.catalog.infrastructure;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_specification")
public class ServiceSpecificationJpaEntity {

    @Id @Column(name = "id") private String id;
    @Column(name = "name") private String name;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "specification_id")
    @OrderBy("id ASC")
    private List<ServiceCharacteristicJpaEntity> characteristics = new ArrayList<>();

    protected ServiceSpecificationJpaEntity() { }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<ServiceCharacteristicJpaEntity> getCharacteristics() { return characteristics; }
}
