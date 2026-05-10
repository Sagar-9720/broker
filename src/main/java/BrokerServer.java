/*
 *TCP Broker Server
 *Accept Producer connection at socker 8888
 * Routes events through shared queue to consumer
 */

public class BrokerServer {

    public BrokerServer() {
    }

    public static void main(String[] args) {
        Event e1 = new Event(System.nanoTime(), 123L, 1000.50, 100, (byte) 0, (byte) 0);
        EventQueue q = new EventQueue();
        q.enqueue(e1);
        assert q.size() == 1;
        Event e2 = q.dequeue();
        assert e2.equals(e1);
        assert q.size() == 0;
    }


}
