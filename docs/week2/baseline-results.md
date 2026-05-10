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

## Experiment: Removed External ReentrantLock

### Previous Design

ConcurrentLinkedQueue was wrapped with an external ReentrantLock.

Architecture:

Producer -> ConcurrentLinkedQueue + ReentrantLock -> Consumer

### Change Applied

Removed:
- ReentrantLock
- lock/unlock calls around enqueue()
- lock/unlock calls around dequeue()
- lock around size()

Current architecture:

Producer -> ConcurrentLinkedQueue -> Consumer

### Results After Change

Consumer Throughput:
~6.1M to 7.4M events/sec

Queue Size:
Typically between 100 to 1000 events

Observed Peak Queue:
~41K events during temporary throughput drops

### Before vs After

Before:
- Throughput unstable
- Queue continuously growing into millions
- Severe backlog accumulation
- Producer heavily outrunning consumer

After:
- Throughput significantly smoother
- Queue remains mostly stable
- Consumer nearly keeps pace with producer
- No runaway queue growth observed

### Key Observations

1. External locking severely reduced queue throughput.
2. ConcurrentLinkedQueue already provides thread-safe operations internally.
3. Additional locking introduced unnecessary contention.
4. Removing the lock dramatically improved system stability.
5. Throughput improved from ~2M-4M/sec range to ~6M-7M/sec range.
6. Queue equilibrium improved substantially.

### Engineering Insight

Small synchronization mistakes can massively impact throughput in high-frequency systems.

Using:
ConcurrentLinkedQueue + ReentrantLock

created double synchronization overhead and serialized queue access unnecessarily.

### Remaining Bottlenecks

Flamegraph analysis shows remaining hotspots in:
- ConcurrentLinkedQueue operations
- Event deserialization
- ByteBuffer operations
- GC activity from object allocation

### Next Planned Investigation

1. Analyze allocation pressure
2. Reduce serialization overhead
3. Replace ConcurrentLinkedQueue with RingBuffer
4. Explore lock-free bounded queue design

## Multi-Producer Stress Test

### Configuration

- 3 concurrent producer clients
- single consumer thread
- ConcurrentLinkedQueue
- Java 24
- G1GC
- Heap: -Xms2g -Xmx2g

### Observed Throughput

Consumer throughput fluctuated between:

~3.5M to ~6.3M events/sec

Temporary drops as low as:
~32K events/sec

### Queue Behavior

Observed queue spikes:
~786K events

Typical behavior:
- queue rapidly expands under burst load
- consumer later drains backlog aggressively
- throughput oscillates during queue buildup

### Important Observation

Average throughput remained high even during periods where queue depth exploded.

This indicates:
throughput alone is insufficient to evaluate broker health because latency likely increases dramatically during backlog accumulation.

### GC Interpretation

Heap repeatedly expanded close to:
~1.2GB

and returned near:
~1MB

after Young GC cycles.

Example:

1228M -> 1M

This strongly indicates:
- extremely high short-lived allocation rate
- temporary object churn
- queue node allocation pressure
- transient serialization/deserialization garbage

### Flamegraph Analysis

Primary hotspots observed:
- ConcurrentLinkedQueue.offer
- ConcurrentLinkedQueue.poll
- EventSerializer.deserialize
- ByteBuffer operations
- G1 evacuation/copying paths

This suggests:
- queue coordination overhead is significant
- allocation pressure is substantial
- serialization work is non-trivial
- networking is not currently the primary bottleneck

### Synchronization Insight

ConcurrentLinkedQueue already implements internal non-blocking synchronization.

Wrapping it with ReentrantLock effectively serialized queue access and removed much of the queue's concurrency advantage.

Removing the external lock:
- dramatically improved throughput
- stabilized queue depth
- reduced producer contention
- improved consumer equilibrium

### Architectural Conclusion

Current performance depends heavily on:
- JVM allocation efficiency
- G1GC throughput
- temporary object cleanup

As producer concurrency increases further, the current architecture is expected to suffer from:
- increased queue contention
- latency instability
- larger backlog accumulation
- higher GC CPU utilization

### Scalability Limitations

Current design limitations:
- unbounded queue growth
- allocation-heavy message flow
- temporary ByteBuffer churn
- no backpressure mechanism
- queue coordination overhead

### Engineering Direction

The current benchmark justifies transition toward:
- bounded RingBuffer architecture
- lock-free sequencing
- reusable buffers
- reduced allocations
- cache-friendly memory layout
- explicit backpressure handling

### Future Measurements

Future benchmark targets:
- p99 latency
- allocation rate/sec
- GC CPU percentage
- producer-to-consumer lag
- queue wait time
- cache miss analysis
- lock contention profiling


## Lock Profiling Result

Lock profiling after removing the external ReentrantLock showed negligible observable lock contention.

The generated lock flamegraph was effectively empty, indicating:
- producer threads were no longer significantly blocked on synchronization
- queue access was no longer serialized externally
- JVM monitor contention was minimal under concurrent load

This confirms that removing the external ReentrantLock successfully eliminated the dominant synchronization bottleneck from the baseline architecture.