# GC Notes

## JVM Configuration

GC: G1GC
Heap Size:
- Initial Heap: 2GB
- Max Heap: 2GB

CPU:
- 8 cores available

## Observations

The broker generates extremely high allocation pressure under sustained load.

Frequent Young Generation GC pauses were observed throughout execution.

Most collections were:
Pause Young (Normal) (G1 Evacuation Pause)

## GC Frequency

Young GC events occurred very frequently during high throughput processing.

Observed:
GC(0) through GC(21)
within roughly 28 seconds of runtime.

This indicates aggressive short-lived object allocation.

## Pause Times

Typical pause times:
~0.8ms to ~10ms

Largest observed pause:
~10.8ms

Most pauses remained relatively low latency.

Examples:
- 2.9ms
- 1.1ms
- 6.7ms
- 10.8ms
- 9.3ms

## Heap Behavior

Heap repeatedly expanded Eden regions aggressively:
- 102MB
- 371MB
- 677MB
- 1107MB
- 1227MB

After each young collection:
heap usage dropped back to ~1MB.

Example:
1097M -> 1M
1228M -> 1M

This strongly suggests:
- most allocated objects are extremely short-lived
- objects die young
- allocation rate is massive

## Important Insight

The system is not leaking memory.

Instead:
the broker is continuously allocating temporary objects that quickly become garbage.

Likely sources:
- Event object creation
- ByteBuffer.allocate()
- temporary byte arrays
- queue node allocations
- serialization/deserialization buffers

## Flamegraph Correlation

Flamegraph analysis also showed hotspots in:
- ByteBuffer operations
- EventSerializer.deserialize
- ConcurrentLinkedQueue
- G1 allocation paths

GC logs validate the flamegraph findings.

## Engineering Conclusions

Current architecture is allocation-heavy.

The broker depends heavily on JVM young-generation cleanup to sustain throughput.

At current scale:
GC pauses are still manageable,
but allocation pressure will become a larger bottleneck as throughput increases.

## Future Optimizations

Potential improvements:
- reusable buffers
- object pooling
- preallocated ring buffer
- reduced temporary allocations
- direct/off-heap buffers
- lock-free bounded queue