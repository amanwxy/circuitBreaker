package com.example.demo.lld.circuitbreaker.state;

import com.example.demo.lld.circuitbreaker.core.CircuitBreakerOrchestrator;
import java.time.Instant;

/** Lets a few probe requests through before deciding whether to recover or reopen. */
public final class HalfOpenState implements CircuitBreakerState {
    @Override
    public boolean allowRequest(CircuitBreakerOrchestrator breaker, Instant now) {
        return breaker.incrementHalfOpenProbeCount() <= breaker.getConfig().halfOpenThreshold();
    }

    @Override
    public void onSuccess(CircuitBreakerOrchestrator breaker, Instant now) {
        if (breaker.incrementHalfOpenSuccessCount() >= breaker.getConfig().successThreshold()) {
            breaker.close();
        } else if (breaker.getHalfOpenProbeCount() >= breaker.getConfig().halfOpenThreshold()) {
            breaker.open(now);
        }
    }

    @Override
    public void onFailure(CircuitBreakerOrchestrator breaker, Instant now) {
        breaker.open(now);
    }

    @Override
    public String name() {
        return "HALF_OPEN";
    }
}
