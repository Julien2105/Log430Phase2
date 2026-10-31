package com.candronex.inventory.domain;

/** Port de sortie : piste d'audit sous forme de journaux structurés et corrélés (aucune table d'audit). */
public interface AuditTrail {
    void record(String event, String clientId, String orderId, String detail);
}
