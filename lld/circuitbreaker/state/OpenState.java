package com.example.demo.lld.circuitbreaker.state;

import com.example.demo.lld.circuitbreaker.core.CircuitBreakerOrchestrator;
import java.time.Instant;

/** Requests are rejected until the cool-down period finishes. */
public final class OpenState implements CircuitBreakerState {
    @Override
    public boolean allowRequest(CircuitBreakerOrchestrator breaker, Instant now) {
        if (breaker.canTryHalfOpen(now)) {
            breaker.halfOpen();
            return breaker.getState().allowRequest(breaker, now);
        }
        return false;
    }

    @Override
    public void onSuccess(CircuitBreakerOrchestrator breaker, Instant now) {
        // OPEN never permits a request, so there is no result to process.
    }

    @Override
    public void onFailure(CircuitBreakerOrchestrator breaker, Instant now) {
        // OPEN never permits a request, so there is no result to process.
    }

    @Override
    public String name() {
        return "OPEN";
    }
}
