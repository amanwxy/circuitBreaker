package com.example.demo.lld.circuitbreaker.facade;

import com.example.demo.lld.circuitbreaker.config.CircuitBreakerConfig;
import com.example.demo.lld.circuitbreaker.core.CircuitBreakerOrchestrator;
import com.example.demo.lld.circuitbreaker.metrics.CircuitBreakerMetrics;

import java.time.Clock;
import java.time.Instant;

/** Small public API for callers; hides state-transition details. */
public final class CircuitBreakerFacade {
    private final CircuitBreakerOrchestrator orchestrator;
    private final CircuitBreakerMetrics metrics = new CircuitBreakerMetrics();
    private final Clock clock;

    public CircuitBreakerFacade(CircuitBreakerConfig config) {
        this(config, Clock.systemUTC());
    }

    public CircuitBreakerFacade(CircuitBreakerConfig config, Clock clock) {
        this.orchestrator = new CircuitBreakerOrchestrator(config);
        this.clock = clock;
    }

    /** Executes the protected service call and records its outcome. */
    public <T> T executeCircuitBreaker(CircuitBreakerRequest<T> request) {
        Instant now = clock.instant();
        if (!orchestrator.allowRequest(now)) {
            metrics.recordRejected(now);
            throw new CircuitBreakerOpenException();
        }

        try {
            T response = request.execute();
            metrics.recordSuccess(now);
            orchestrator.recordSuccess(now);
            return response;
        } catch (Exception exception) {
            metrics.recordFailure(now);
            orchestrator.recordFailure(now);
            throw new RuntimeException("Protected service request failed", exception);
        }
    }

    public String getCircuitBreakerState() {
        return orchestrator.getState().name();
    }

    public CircuitBreakerMetrics getMetrics() {
        return metrics;
    }
}
