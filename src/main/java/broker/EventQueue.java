package broker;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class EventQueue {
    private final Queue<Event> queue;
    private final AtomicLong enqueueCount = new AtomicLong();
    private final AtomicLong dequeueCount = new AtomicLong();

    public EventQueue() {
        this.queue = new ConcurrentLinkedQueue<>();
    }

    /*
    Add Event to Queue(Operation for producer)
     */
    public void enqueue(Event event) {

        queue.add(event);
        enqueueCount.incrementAndGet();

    }

    /*
    Remove Event from Queue(Operation for consumer)
     */
    public Event dequeue() {
        Event event = queue.poll();
        if (event != null) dequeueCount.incrementAndGet();
        return event;

    }

    /**
     * Returns approximate size (for monitoring)
     */
    public int size() {

        return queue.size();
    }

    public long getEnqueueCount() {
        return enqueueCount.get();
    }

    public long getDequeueCount() {
        return dequeueCount.get();
    }

}

