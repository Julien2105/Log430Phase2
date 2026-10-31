package com.candronex.order.infrastructure;

import com.candronex.order.domain.AuditTrail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class Slf4jAuditTrail implements AuditTrail {

    private static final Logger LOG = LoggerFactory.getLogger("audit");

    @Override
    public void record(String event, String clientId, String orderId, String detail) {
        String previousClient = MDC.get("clientId");
        String previousOrder = MDC.get("orderId");
        if (clientId != null) MDC.put("clientId", clientId);
        if (orderId != null) MDC.put("orderId", orderId);
        try {
            LOG.info("event={} detail={}", event, detail == null ? "-" : detail);
        } finally {
            restore("clientId", previousClient);
            restore("orderId", previousOrder);
        }
    }

    private static void restore(String key, String previous) {
        if (previous == null) MDC.remove(key); else MDC.put(key, previous);
    }
}
