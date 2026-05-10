# Broker

High-throughput event broker built in Java to study:
- TCP networking
- binary serialization
- queue architectures
- concurrency
- profiling
- GC behavior
- systems performance optimization

## Current Architecture

Producer Client
-> TCP Socket
-> Broker Server
-> Event Deserialization
-> ConcurrentLinkedQueue
-> Consumer Thread

## Features

- TCP producer/server communication
- custom binary protocol
- framed message parsing
- multi-threaded producer handling
- shared concurrent queue
- throughput monitoring
- async-profiler flamegraphs
- GC analysis

## Current Performance

Before removing external lock:
~2M to 4M events/sec

After removing external lock:
~6M to 7M events/sec

## Key Findings

- External locking severely reduced throughput.
- ConcurrentLinkedQueue already provides thread safety.
- Serialization and allocation pressure are major bottlenecks.
- Current architecture creates large amounts of short-lived garbage.

## Repository Structure

docs/
- week1/
- week2/

benchmarks/
- flamegraphs/
- gc-logs/
- csv/

src/main/java/
- BrokerServer.java
- ProducerClient.java
- EventSerializer.java
- EventQueue.java

## Running the Broker

Build:
./gradlew build

Start Broker:
java -ea -cp build/classes/java/main BrokerServer

Start Producer:
java -ea -cp build/classes/java/main ProducerClient

## Profiling

Generate flamegraph:
asprof -d 20 -f benchmarks/flamegraphs/baseline.html <PID>

Enable GC logging:
java "-Xlog:gc*:file=benchmarks/gc-logs/baseline-gc.log" ...

## Current Bottlenecks

- queue coordination overhead
- allocation-heavy serialization
- temporary object churn
- GC pressure

## Next Goals

- bounded RingBuffer
- lock-free queue
- reduced allocations
- reusable buffers
- cache-friendly architecture