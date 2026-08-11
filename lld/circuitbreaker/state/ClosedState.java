package com.example.demo.lld.circuitbreaker.state;

import com.example.demo.lld.circuitbreaker.core.CircuitBreakerOrchestrator;
import java.time.Instant;

/** Requests are allowed while the downstream service is healthy enough. */
public final class ClosedState implements CircuitBreakerState {
    @Override
    public boolean allowRequest(CircuitBreakerOrchestrator breaker, Instant now) {
        return true;
    }

    @Override
    public void onSuccess(CircuitBreakerOrchestrator breaker, Instant now) {
        breaker.resetConsecutiveFailures();
    }

    @Override
    public void onFailure(CircuitBreakerOrchestrator breaker, Instant now) {
        if (breaker.incrementConsecutiveFailures() >= breaker.getConfig().failureThreshold()) {
            breaker.open(now);
        }
    }

    @Override
    public String name() {
        return "CLOSED";
    }
}
