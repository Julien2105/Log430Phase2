package com.candronex.order.infrastructure;

import java.io.Serializable;
import java.util.Objects;

public class IdempotencyKeyId implements Serializable {

    private String clientId;
    private String idempotencyKey;

    public IdempotencyKeyId() { }

    public IdempotencyKeyId(String clientId, String idempotencyKey) {
        this.clientId = clientId;
        this.idempotencyKey = idempotencyKey;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof IdempotencyKeyId k && Objects.equals(clientId, k.clientId) && Objects.equals(idempotencyKey, k.idempotencyKey);
    }

    @Override
    public int hashCode() { return Objects.hash(clientId, idempotencyKey); }
}
