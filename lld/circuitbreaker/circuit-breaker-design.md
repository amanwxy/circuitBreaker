# Circuit Breaker Design

## Purpose

`CircuitBreakerFacade` protects a remote or unreliable service call. It stops repeatedly calling a failing dependency, gives it time to recover, and then verifies recovery with a small number of probe requests.

The public API is deliberately small:

```java
T executeCircuitBreaker(CircuitBreakerRequest<T> request)
String getCircuitBreakerState()
CircuitBreakerMetrics getMetrics()
```

## Main Components

| Component | Responsibility |
| --- | --- |
| `CircuitBreakerFacade` | Public entry point. Runs the protected request and records its result. |
| `CircuitBreakerOrchestrator` | Owns the current state and coordinates state transitions. |
| `CircuitBreakerState` | State-pattern interface implemented by `ClosedState`, `OpenState`, and `HalfOpenState`. |
| `CircuitBreakerConfig` | Holds failure, half-open, success thresholds and the open duration. |
| `CircuitBreakerMetrics` | Tracks request number, successes, failures, and the most recent timestamp. |
| `CircuitBreakerRequest<T>` | A functional interface representing the service call to protect. |

## State Flow

```text
                    failures reach failureThreshold
        CLOSED  ------------------------------------>  OPEN
          ^                                               |
          |                                               | openDuration elapsed
          |                                               v
          |                                         HALF_OPEN
          |                                           |      |
          | successThreshold successful probes       |      | failed probe
          +------------------------------------------+      +----> OPEN
```

### CLOSED

Requests are executed. The breaker counts consecutive failures. Once the count reaches `failureThreshold`, it moves to `OPEN`. A successful request resets the consecutive-failure count.

### OPEN

Requests are rejected immediately with `CircuitBreakerOpenException`; the protected service is not called. After `openDuration`, the next request moves the breaker to `HALF_OPEN` and is allowed as a probe.

### HALF_OPEN

Only up to `halfOpenThreshold` probe requests are allowed. If `successThreshold` probes succeed, the breaker closes. Any failed probe immediately reopens it.

## Configuration

```java
CircuitBreakerConfig config = new CircuitBreakerConfig(
        3,                      // failureThreshold
        2,                      // halfOpenThreshold: maximum probes
        2,                      // successThreshold: successful probes needed to close
        Duration.ofSeconds(30)  // openDuration
);
```

`successThreshold` must not be greater than `halfOpenThreshold`.

## Usage

Wrap only the external service call. The facade records a success when the callback returns and records a failure when it throws.

```java
CircuitBreakerFacade breaker = new CircuitBreakerFacade(config);

try {
    PaymentResponse response = breaker.executeCircuitBreaker(
            () -> paymentClient.charge(paymentRequest));
    // Use response
} catch (CircuitBreakerOpenException exception) {
    // Return a fallback, enqueue for retry, or tell the caller to try later.
} catch (RuntimeException exception) {
    // The protected service was called but failed.
}

System.out.println(breaker.getCircuitBreakerState());

CircuitBreakerMetrics metrics = breaker.getMetrics();
System.out.printf("requests=%d, successes=%d, failures=%d%n",
        metrics.getRequestNo(),
        metrics.getSuccessCount(),
        metrics.getFailureCount());
```

## What `executeCircuitBreaker` Returns

- When the breaker allows the call and the service succeeds, it returns the service response.
- When the breaker is `OPEN`, it throws `CircuitBreakerOpenException` without invoking the callback.
- When the callback fails, it records the failure, updates the state if necessary, and throws a `RuntimeException` whose cause is the original exception.

## Scope and Extensions

This version is intentionally interview-friendly. It does not include retries, fallbacks, a sliding time window, distributed/shared metrics, persistence, or asynchronous execution. Those can be added later without changing the facade API.

For production use, consider per-dependency breaker instances, structured logging/metrics export, timeout handling, retry policy, and a rolling failure-rate window rather than only consecutive failures.
