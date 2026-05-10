import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 *TCP Broker Server
 *Accept Producer connection at socker 8888
 * Routes events through shared queue to consumer
 */
public class BrokerServer {
    private static final int PORT = 8888;
    private static final int FIXED_THREAD_POOL_SIZE = 10;
    private static final int PRODUCER_THREADS = 1;
    private static final int NUM_PRODUCERS = 1;

    private final EventQueue eventQueue;
    private final ExecutorService threadPool;
    private volatile boolean running = true;
    private long startTime;

    public BrokerServer() {
        this.eventQueue = new EventQueue();
        this.threadPool = Executors.newFixedThreadPool(FIXED_THREAD_POOL_SIZE);
    }

    public static void main(String[] args) throws IOException {
        BrokerServer broker = new BrokerServer();
        broker.start();
    }

    public void start() throws IOException {
        startTime = System.currentTimeMillis();
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("[Broker] Started on port " + PORT);

        //Consumer Thread
        Thread consumer = new Thread(this::consumeEvents, "Consumer");
        consumer.setDaemon(true);
        consumer.start();

        // Accept Producer Connections
        Thread acceptor = new Thread(() -> {
            try {
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.execute(() -> handleProducer(clientSocket));
                }
            } catch (IOException e) {
                if (running) e.printStackTrace();
            }
        }, "Acceptor");
        acceptor.start();
    }

    /*
     * Handle incoming producer connection
     */
    private void handleProducer(Socket socket) {
        try (InputStream in = socket.getInputStream()) {
            ByteBuffer buffer = ByteBuffer.allocate(16 * 1024);//16 kb read buffer
            byte[] readBuf = new byte[4096];
            while (running) {
                int read = in.read(readBuf);
                if (read == -1) break;//Connection closed
                buffer.put(readBuf, 0, read);
                buffer.flip();

                //Process all complete frames in buffer
                byte[] frame;
                while ((frame = EventSerializer.readFrame(buffer)) != null) {
                    Event event = EventSerializer.deserialize(frame);
                    eventQueue.enqueue(event);
                }

                //Compact Buffer for next read
                buffer.compact();
            }
        } catch (IOException e) {
            System.err.println("[Producer] Connection closed: " + e.getMessage());
        }
    }

    /*
     * Consumer Thread - process events from queue
     */
    private void consumeEvents() {
        long lastPrintTime = System.currentTimeMillis();
        long lastCount = 0;
        while (running) {
            Event event = eventQueue.dequeue();
            if (event == null) {
                //Spin or Sleep to avoid busy-waiting
                Thread.yield();
                continue;
            }

            //Simulate minimal processing (order book update, etc.)
            //processEvent(event); //placeholder

            // Log throughput every second
            long now = System.currentTimeMillis();
            if (now - lastPrintTime >= 1000) {
                long dequeued = eventQueue.getDequeueCount();
                long eventsPerSec = (dequeued - lastCount);
                System.out.printf("[Consumer] Throughput: %,d events/sec, Queue size: %d\n", eventsPerSec, eventQueue.size());
                lastCount = dequeued;
                lastPrintTime = now;
            }
        }
    }

}

