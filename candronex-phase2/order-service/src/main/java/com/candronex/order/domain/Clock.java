package com.candronex.order.domain;

import java.time.Instant;

/** Port de sortie : source de temps (substituable dans les tests). */
public interface Clock {
    Instant now();
}
