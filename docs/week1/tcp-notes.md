# TCP Notes

## What is TCP?

TCP is a stream-oriented transport protocol.
It guarantees:
- ordered delivery
- reliable delivery
- retransmission on packet loss

TCP does NOT preserve message boundaries.

Applications must define their own framing protocol.

## Why TCP Framing Is Required

TCP delivers bytes as a continuous stream.

Example:

If sender sends:
[Message A][Message B]

Receiver may get:
- A only
- A + partial B
- half of A
- A + B together

Because TCP works as a byte stream, not as discrete messages.

## Problem Without Framing

Without framing:
receiver cannot know:
- where one message ends
- where next message begins

This causes corrupted parsing.

## Current Broker Framing Protocol

Current format:

[4-byte size][event payload]

Payload contains:
- timestamp
- orderId
- price
- quantity
- eventType
- side

The size header tells receiver exactly how many bytes belong to one event.

## Why readFrame() Exists

readFrame() solves TCP fragmentation.

Steps:
1. Read first 4 bytes (message size)
2. Check if full payload exists in buffer
3. If incomplete:
   wait for more socket reads
4. If complete:
   extract exactly one message

This prevents partial message corruption.

## Why ByteBuffer Is Used

ByteBuffer provides:
- binary read/write operations
- explicit byte ordering
- compact memory layout
- efficient parsing

Used for:
- serialization
- deserialization
- socket buffering

## Key Learning

TCP guarantees reliable byte delivery,
not application-level message delivery.

Applications must implement framing themselves.