/*
 * Test Producer - connects to broker and sends events
 */

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

public class ProducerClient {
    private final String host;
    private final int port;
    private volatile boolean running = true;

    public ProducerClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) throws IOException {
        ProducerClient client = new ProducerClient("localhost", 8888);
        client.connect();

    }

    public void connect() throws IOException {
        Socket socket = new Socket(host, port);
        OutputStream out = socket.getOutputStream();
        BufferedOutputStream bufferedOut = new BufferedOutputStream(out);

        Random random = new Random();
        long eventsSent = 0;
        long startTime = System.currentTimeMillis();
        System.out.println("[Producer] Connected to broker at " + host + ":" + port);

        try {
            while (running) {
                //Create Event
                Event event = new Event(System.nanoTime(), random.nextLong() * 1000000L, 1000.0 + random.nextDouble() * 1000.0, random.nextInt(1000) + 1, (byte) 0, //BUY Order
                        (byte) (random.nextInt(2)));

                //Serialize and send
                byte[] serializedEvent = EventSerializer.serialize(event);
                bufferedOut.write(serializedEvent);
                eventsSent++;

                //FLush periodically
                if (eventsSent % 1000 == 0) {
                    bufferedOut.flush();
                }

                //Log stats
                if (eventsSent % 100000 == 0) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    long rate = (eventsSent * 1000) / elapsedTime;
                    System.out.printf("[Producer] Sent %,d events @ %,d/sec\n", eventsSent, rate);
                }
            }
        } finally {
            bufferedOut.close();
            socket.close();
        }


    }
}
