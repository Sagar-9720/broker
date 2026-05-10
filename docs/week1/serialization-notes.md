# Serialization Notes

## What is Serialization?

Serialization is the process of converting an object into binary data for:
- network transport
- storage
- inter-process communication

Deserialization reconstructs the object from binary data.

## Why Serialization Is Required

Sockets transmit bytes, not Java objects.

The broker must convert:
Event object -> byte[]

before sending over TCP.

Receiver converts:
byte[] -> Event object

## Current Event Binary Layout

Current frame format:

[4-byte size]
[8-byte timestamp]
[8-byte orderId]
[8-byte price]
[4-byte quantity]
[1-byte eventType]
[1-byte side]

Total payload size:
30 bytes

Total frame size:
34 bytes including size prefix.

## Why Binary Instead of JSON

Binary serialization is:
- smaller
- faster
- cache-friendly
- avoids string parsing overhead

JSON would:
- increase payload size
- create more allocations
- require text parsing

Binary protocols are commonly used in:
- trading systems
- brokers
- databases
- distributed systems

## Why ByteBuffer Is Used

ByteBuffer provides:
- primitive binary operations
- compact memory handling
- sequential writes/reads
- controlled buffer positions

Examples:
- putLong()
- putDouble()
- getInt()

## Current Serialization Flow

Producer:
Event -> serialize() -> byte[] -> socket write

Broker:
socket read -> readFrame() -> deserialize() -> Event

## Current Bottlenecks

Flamegraph and GC analysis showed:
- ByteBuffer.allocate()
- deserialize()
- temporary byte[] allocations

are hot paths during high throughput.

Current design creates large amounts of short-lived garbage.

## Important Learning

Serialization format design directly affects:
- throughput
- latency
- memory allocation
- GC pressure

Efficient binary protocols are critical in high-performance systems.