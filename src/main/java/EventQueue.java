import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

public class EventQueue {
    private final Queue<Event> queue;
    private final ReentrantLock lock = new ReentrantLock();
    private volatile long enqueueCount;
    private volatile long dequeueCount;

    public EventQueue() {
        this.queue = new ConcurrentLinkedQueue<>();
    }

    /*
    Add Event to Queue(Operation for producer)
     */
    public void enqueue(Event event) {
        lock.lock();
        try {
            queue.add(event);
            enqueueCount++;
        } finally {
            lock.unlock();
        }
    }

    /*
    Remove Event from Queue(Operation for consumer)
     */
    public Event dequeue() {
        lock.lock();
        try {
            Event event = queue.poll();
            if (event != null) dequeueCount++;
            return event;

        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns approximate size (for monitoring)
     */
    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }

    public long getEnqueueCount() {
        return enqueueCount;
    }

    public long getDequeueCount() {
        return dequeueCount;
    }

}
