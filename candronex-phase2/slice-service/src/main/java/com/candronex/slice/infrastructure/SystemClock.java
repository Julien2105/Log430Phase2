package com.candronex.slice.infrastructure;

import com.candronex.slice.domain.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SystemClock implements Clock {
    @Override
    public Instant now() { return Instant.now(); }
}
