# Profiling Notes

## Major Hotspots

1. ConcurrentLinkedQueue.add
2. ConcurrentLinkedQueue.poll
3. EventSerializer.deserialize
4. ByteBuffer operations
5. G1 GC operations

## Observations

- Queue operations consume significant CPU time.
- Event deserialization appears on hot path.
- Frequent allocations are triggering G1 collection work.
- Socket read operations are visible but not dominant.
- System is allocation-heavy.

## Bottleneck Hypothesis

Primary bottlenecks are likely:
1. Queue coordination overhead
2. Object allocation pressure
3. Serialization allocations

## Future Optimizations

- RingBuffer
- Object pooling
- Preallocated buffers
- Reduced allocations