package com.candronex.inventory.support;

import com.candronex.inventory.domain.AuditTrail;
import java.util.ArrayList;
import java.util.List;

public class RecordingAuditTrail implements AuditTrail {

    public final List<String> events = new ArrayList<>();

    @Override
    public void record(String event, String clientId, String orderId, String detail) { events.add(event); }
}
