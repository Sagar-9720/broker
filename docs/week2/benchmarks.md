# Benchmark Results - Week 2

## Benchmark Environment

Machine:
MacBook Air

CPU:
8 cores

Java Version:
Java 24

Heap Configuration:
-Xms2g -Xmx2g

GC:
G1GC

JMH Version:
1.36

Benchmark Mode:
Throughput + Sampling Time

Warmup:
2 iterations × 5s

Measurement:
3 iterations × 10s

## Broker Configuration

Queue:
ConcurrentLinkedQueue

Synchronization:
Initial version used external ReentrantLock around queue operations

Consumer Model:
Single consumer thread

Producer Model:
Single producer client

Socket Type:
Blocking TCP sockets

Read Buffer:
16KB ByteBuffer

## Throughput Results

### Before Removing External Lock

Consumer Throughput:
~2M to 4M events/sec

Queue Behavior:
Queue size continuously increased into millions.

Observed Queue Peaks:
~8.8 million events

System Behavior:
Consumer could not consistently keep up with producer.

Primary Bottleneck:
External lock contention around queue operations.

### After Removing External Lock

Consumer Throughput:
~6M to 7M events/sec

Typical Queue Size:
100 to 1000 events

Observed Peak Queue:
~41K events

System Behavior:
Queue remained mostly stable and consumer nearly matched producer throughput.

Primary Improvement:
Removing ReentrantLock reduced serialization of producer threads and improved queue concurrency.

## JMH Benchmark Results

### Benchmark: singleProducerSingleConsumer

Warmup Results:
- 290.280 ops/sec
- 307.918 ops/sec

Measurement Results:
- Iteration 1: 280.050 ops/sec
- Iteration 2: 240.525 ops/sec
- Iteration 3: 218.522 ops/sec

Average Throughput:
246.365 ops/sec

Observed Trend:
Performance degraded during execution due to allocation pressure and GC overhead.

### Benchmark: enqueueLatency

Latency Distribution:
- p50: 0.083 us
- p90: 0.084 us
- p95: 0.167 us
- p99: 0.459 us
- p99.9: 31.002 us
- p99.99: 425.734 us

Maximum Observed Latency:
4051697.664 us (~4 seconds)

Observed Failure:
Benchmark eventually terminated with:
java.lang.OutOfMemoryError

## Allocation Profiling Results

Major Allocation Sources:
- broker.Event
- ConcurrentLinkedQueue$Node
- byte[]
- ByteBuffer allocations

Key Observation:
Every enqueue operation creates additional garbage through queue node allocation and temporary serialization buffers.

Impact:
High allocation rate caused continuous Young GC cycles and throughput degradation.

## GC Results

Observed:
- frequent Young GC events
- short pause durations
- high allocation rate
- evacuation-heavy G1 behavior

Typical GC Pause:
~1ms to ~10ms

Largest Observed Pause:
~10.8ms

Heap Pattern:
Large Eden allocations followed by rapid cleanup.

Example:
1228M -> 1M

Observed GC Hotspots:
- G1YoungCollector
- do_copy_to_survivor_space
- attempt_allocation_slow
- evacuation tasks

Conclusion:
GC overhead became a major runtime cost due to allocation-heavy architecture.

## Profiling Summary

Hot methods identified:
- ConcurrentLinkedQueue.add
- ConcurrentLinkedQueue.offer
- ConcurrentLinkedQueue.poll
- EventSerializer.deserialize
- ByteBuffer.allocate
- SocketInputStream.read

CPU Flamegraph Findings:
- queue coordination consumed major CPU time
- allocation pressure dominated runtime
- GC activity occupied significant CPU percentage
- lock contention became relatively small after lock removal

Allocation Flamegraph Findings:
Heavy allocation activity from:
- queue node creation
- Event object creation
- temporary byte arrays

Lock Profile Findings:
Very little remaining lock contention after removing external ReentrantLock.

## Key Findings

1. External locking caused severe contention.
2. Removing ReentrantLock significantly improved throughput.
3. Current system creates large amounts of short-lived garbage.
4. Serialization and queue coordination are major CPU consumers.
5. Networking is not yet the primary bottleneck.
6. Allocation rate and GC pressure are now larger bottlenecks than locking.
7. ConcurrentLinkedQueue node allocation creates substantial heap churn.
8. Throughput degrades over time as GC pressure increases.
9. Tail latency becomes unstable under sustained allocation pressure.
10. Unbounded queue growth eventually causes memory exhaustion.

## Current Limitations

- allocation-heavy architecture
- unbounded queue
- busy-spin consumer
- temporary ByteBuffer allocations
- no backpressure
- per-node queue allocations
- excessive short-lived objects
- unstable tail latency under sustained load

## Next Optimization Targets

- bounded RingBuffer
- reusable buffers
- lock-free queue
- reduced allocations
- improved cache locality
- preallocated event storage
- false-sharing reduction
- wait strategy optimization
- SPSC/MPSC ring-buffer architecture

## Final Week 2 Conclusion

The baseline broker implementation is functional but heavily allocation-bound.

Initial lock contention was mitigated by removing external synchronization around ConcurrentLinkedQueue operations, producing a major throughput improvement.

After removing the lock bottleneck, the dominant limitations shifted to:
- allocation overhead
- GC activity
- queue node creation
- temporary buffer allocation

The current architecture is suitable as a learning baseline but not suitable for low-latency production workloads.

The next architectural transition is replacing ConcurrentLinkedQueue with a bounded lock-free RingBuffer to:
- eliminate queue node allocations
- reduce GC pressure
- stabilize latency
- improve cache locality
- increase throughput consistency
- prevent unbounded memory growth