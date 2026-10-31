package com.candronex.order.infrastructure;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "order_line")
public class OrderLineJpaEntity {

    @Id @Column(name = "id") private UUID id;
    @ManyToOne @JoinColumn(name = "order_id") private OrderJpaEntity order;
    @Column(name = "action") private String action;
    @Column(name = "drone_id") private String droneId;
    @Column(name = "service_offering_id") private String serviceOfferingId;

    @ElementCollection
    @CollectionTable(name = "order_line_characteristic", joinColumns = @JoinColumn(name = "line_id"))
    private List<CharacteristicEmbeddable> characteristics = new ArrayList<>();

    protected OrderLineJpaEntity() { }

    public OrderLineJpaEntity(UUID id, OrderJpaEntity order, String action, String droneId, String serviceOfferingId,
                              List<CharacteristicEmbeddable> characteristics) {
        this.id = id;
        this.order = order;
        this.action = action;
        this.droneId = droneId;
        this.serviceOfferingId = serviceOfferingId;
        this.characteristics = new ArrayList<>(characteristics);
    }

    public UUID getId() { return id; }
    public String getAction() { return action; }
    public String getDroneId() { return droneId; }
    public String getServiceOfferingId() { return serviceOfferingId; }
    public List<CharacteristicEmbeddable> getCharacteristics() { return characteristics; }
}
