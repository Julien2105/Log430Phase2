package com.candronex.drone.infrastructure;

import com.candronex.drone.domain.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SystemClock implements Clock {
    @Override
    public Instant now() { return Instant.now(); }
}
