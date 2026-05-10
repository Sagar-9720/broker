# Profiling Results - Week 2 Baseline

## Test Environment

Machine:
MacBook Air

CPU Cores:
8

JVM:
Java 24

Heap:
-Xms2g -Xmx2g

GC:
G1GC

## Current Architecture

Producer
-> TCP Socket
-> EventSerializer.deserialize()
-> ConcurrentLinkedQueue
-> Consumer Thread

## Baseline Throughput

Before removing external lock:
~2M to 4M events/sec

After removing external lock:
~6M to 7M events/sec

## Queue Behavior

Before optimization:
- queue size continuously increased
- backlog reached millions of events

After optimization:
- queue remained mostly stable
- queue depth usually below 1000

## Flamegraph Findings

Major hotspots observed:
- ConcurrentLinkedQueue.add
- ConcurrentLinkedQueue.poll
- EventSerializer.deserialize
- ByteBuffer operations
- SocketInputStream.read
- G1 GC allocation paths

## GC Findings

Observed:
- frequent Young GC pauses
- high allocation rate
- temporary object churn

Typical pauses:
~1ms to ~10ms

Heap repeatedly expanded Eden regions and returned close to 1MB after collection.

Example:
1228M -> 1M

This indicates:
most objects are short-lived.

## Major Bottlenecks Identified

1. Queue coordination overhead
2. Serialization allocations
3. ByteBuffer allocation pressure
4. GC overhead from temporary objects

## Important Discovery

Removing external ReentrantLock dramatically improved:
- throughput
- queue stability
- system equilibrium

ConcurrentLinkedQueue already handled synchronization internally.

Additional locking created severe contention.

## Engineering Conclusions

Current broker is allocation-heavy.

System performance is now limited more by:
- allocation pressure
- queue overhead
  than raw socket I/O.

Networking itself is not currently the primary bottleneck.

## Next Planned Optimizations

- bounded RingBuffer
- reduced allocations
- reusable buffers
- lock-free structures
- cache-friendly queue design