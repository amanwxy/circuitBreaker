package com.example.demo.lld.circuitbreaker.metrics;

import java.time.Instant;

/** Tracks every request handled by one circuit breaker instance. */
public final class CircuitBreakerMetrics {
    private long requestNo;
    private long successCount;
    private long failureCount;
    private Instant timestamp;

    public synchronized void recordSuccess(Instant timestamp) {
        requestNo++;
        successCount++;
        this.timestamp = timestamp;
    }

    public synchronized void recordFailure(Instant timestamp) {
        requestNo++;
        failureCount++;
        this.timestamp = timestamp;
    }

    public synchronized void recordRejected(Instant timestamp) {
        requestNo++;
        this.timestamp = timestamp;
    }

    public synchronized long getRequestNo() {
        return requestNo;
    }

    public synchronized long getSuccessCount() {
        return successCount;
    }

    public synchronized long getFailureCount() {
        return failureCount;
    }

    public synchronized Instant getTimestamp() {
        return timestamp;
    }
}
