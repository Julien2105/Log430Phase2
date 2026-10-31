package com.candronex.order.infrastructure;

import com.candronex.order.domain.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SystemClock implements Clock {
    @Override
    public Instant now() { return Instant.now(); }
}
