package com.candronex.drone.support;

import com.candronex.drone.domain.Clock;
import java.time.Instant;

public class FixedClock implements Clock {

    public Instant now = Instant.parse("2026-10-15T18:58:01Z");

    @Override
    public Instant now() { return now; }
}
