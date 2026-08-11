package com.example.demo.lld.circuitbreaker.facade;

public final class CircuitBreakerOpenException extends RuntimeException {
    public CircuitBreakerOpenException() {
        super("Request rejected because the circuit breaker is OPEN");
    }
}
