package com.example.demo.lld.circuitbreaker.facade;

@FunctionalInterface
public interface CircuitBreakerRequest<T> {
    T execute() throws Exception;
}
