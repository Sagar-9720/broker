/*
 *TCP Broker Server
 *Accept Producer connection at socker 8888
 * Routes events through shared queue to consumer
 */

public class BrokerServer {

    public BrokerServer() {
    }

    public static void main(String[] args) {
        Event original = new Event(System.nanoTime(), 123L, 1000.50, 100, (byte) 0, (byte) 0);
        byte[] bytes = EventSerializer.serialize(original);
        Event restored = EventSerializer.deserialize(bytes);
        assert original.equals(restored);  // They should be identical
    }


}
