# Baseline Results

## Current Architecture

Producer -> TCP Socket -> Broker -> ConcurrentLinkedQueue -> Consumer

## Observed Metrics

Producer Throughput:
~2.8M to 3.4M events/sec

Consumer Throughput:
~1.8M to 4.2M events/sec

Max Queue Size Observed:
~8.8 million events

## Observations

1. Queue grows continuously under sustained load.
2. Consumer cannot consistently keep up with producer.
3. Memory usage likely increasing due to queue accumulation.
4. Throughput fluctuates significantly.
5. No backpressure mechanism exists.
6. System currently uses ConcurrentLinkedQueue + ReentrantLock.

## Hypothesis

Potential bottlenecks:
- lock contention
- object allocation pressure
- queue implementation overhead
- serialization/deserialization cost
- GC pauses

## Next Investigation

1. Remove unnecessary locking around ConcurrentLinkedQueue
2. Measure GC activity
3. Generate flamegraph
4. Benchmark queue independently