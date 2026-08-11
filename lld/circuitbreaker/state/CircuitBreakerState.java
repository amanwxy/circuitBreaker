package com.example.demo.lld.circuitbreaker.state;

import com.example.demo.lld.circuitbreaker.core.CircuitBreakerOrchestrator;
public interface CircuitBreakerState {
    boolean allowRequest(CircuitBreakerOrchestrator breaker, java.time.Instant now);

    void onSuccess(CircuitBreakerOrchestrator breaker, java.time.Instant now);

    void onFailure(CircuitBreakerOrchestrator breaker, java.time.Instant now);

    String name();
}
