package com.example.demo.lld.circuitbreaker.core;

import com.example.demo.lld.circuitbreaker.config.CircuitBreakerConfig;
import com.example.demo.lld.circuitbreaker.state.CircuitBreakerState;
import com.example.demo.lld.circuitbreaker.state.ClosedState;
import com.example.demo.lld.circuitbreaker.state.HalfOpenState;
import com.example.demo.lld.circuitbreaker.state.OpenState;

import java.time.Instant;

/** Owns transitions between CLOSED, OPEN, and HALF_OPEN states. */
public final class CircuitBreakerOrchestrator {
    private final CircuitBreakerConfig config;
    private CircuitBreakerState state = new ClosedState();
    private Instant openedAt;
    private int halfOpenProbeCount;
    private int halfOpenSuccessCount;
    private int consecutiveFailures;

    public CircuitBreakerOrchestrator(CircuitBreakerConfig config) {
        this.config = config;
    }

    public synchronized boolean allowRequest(Instant now) {
        return state.allowRequest(this, now);
    }

    public synchronized void recordSuccess(Instant now) {
        state.onSuccess(this, now);
    }

    public synchronized void recordFailure(Instant now) {
        state.onFailure(this, now);
    }

    public synchronized CircuitBreakerState getState() {
        return state;
    }

    public CircuitBreakerConfig getConfig() {
        return config;
    }

    public void open(Instant timestamp) {
        state = new OpenState();
        openedAt = timestamp;
        halfOpenProbeCount = 0;
        halfOpenSuccessCount = 0;
    }

    public void halfOpen() {
        state = new HalfOpenState();
        halfOpenProbeCount = 0;
        halfOpenSuccessCount = 0;
    }

    public void close() {
        state = new ClosedState();
        openedAt = null;
        halfOpenProbeCount = 0;
        halfOpenSuccessCount = 0;
        consecutiveFailures = 0;
    }

    public boolean canTryHalfOpen(Instant now) {
        return !now.isBefore(openedAt.plus(config.openDuration()));
    }

    public int incrementHalfOpenProbeCount() {
        return ++halfOpenProbeCount;
    }

    public int getHalfOpenProbeCount() {
        return halfOpenProbeCount;
    }

    public int incrementHalfOpenSuccessCount() {
        return ++halfOpenSuccessCount;
    }

    public int incrementConsecutiveFailures() {
        return ++consecutiveFailures;
    }

    public void resetConsecutiveFailures() {
        consecutiveFailures = 0;
    }
}
