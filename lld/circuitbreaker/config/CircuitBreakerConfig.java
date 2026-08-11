package com.example.demo.lld.circuitbreaker.config;

import java.time.Duration;

/** Thresholds used by the circuit breaker. */
public record CircuitBreakerConfig(
        int failureThreshold,
        int halfOpenThreshold,
        int successThreshold,
        Duration openDuration) {

    public CircuitBreakerConfig {
        if (failureThreshold < 1 || halfOpenThreshold < 1 || successThreshold < 1) {
            throw new IllegalArgumentException("All thresholds must be positive");
        }
        if (successThreshold > halfOpenThreshold) {
            throw new IllegalArgumentException("Success threshold cannot exceed half-open threshold");
        }
        if (openDuration == null || openDuration.isNegative() || openDuration.isZero()) {
            throw new IllegalArgumentException("Open duration must be positive");
        }
    }
}
