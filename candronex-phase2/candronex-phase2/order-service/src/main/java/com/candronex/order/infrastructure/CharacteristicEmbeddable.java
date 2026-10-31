package com.candronex.order.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CharacteristicEmbeddable {

    @Column(name = "name") private String name;
    @Column(name = "value") private String value;

    protected CharacteristicEmbeddable() { }

    public CharacteristicEmbeddable(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() { return name; }
    public String getValue() { return value; }
}
