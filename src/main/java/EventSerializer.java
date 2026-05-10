import java.nio.ByteBuffer;

public class EventSerializer {
    private static final int EVENT_SIZE = Long.BYTES +      // timestamp
            Long.BYTES +      // orderId
            Double.BYTES +    // price
            Integer.BYTES +   // quantity
            Byte.BYTES +      // eventType
            Byte.BYTES;       // side

    /*
    Serialize Event to Binary format
    Frame: [8-byte timestamp][8-byte orderId][8-byte price][4-byte quantity][1-byte type][1-byte side]
     */
    public static byte[] serialize(Event event) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + EVENT_SIZE);
        buffer.putInt(EVENT_SIZE); //size prefix

        //Event data
        buffer.putLong(event.timestamp);
        buffer.putLong(event.orderId);
        buffer.putDouble(event.price);
        buffer.putInt(event.quantity);
        buffer.put(event.eventType);
        buffer.put(event.side);

        return buffer.array();
    }

    /*
    Deserialize binary data to Event object
     */
    public static Event deserialize(byte[] data) {
        if (data.length != Integer.BYTES + EVENT_SIZE) {
            throw new IllegalArgumentException("Invalid Event Object :" + data.length);
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int size = buffer.getInt();
        if (size != EVENT_SIZE) {
            throw new IllegalArgumentException("Invalid Event Size :" + size);
        }
        long timestamp = buffer.getLong();
        long orderId = buffer.getLong();
        double price = buffer.getDouble();
        int quantity = buffer.getInt();
        byte eventType = buffer.get();
        byte side = buffer.get();

        return new Event(timestamp, orderId, price, quantity, eventType, side);
    }

    public static byte[] readFrame(ByteBuffer buffer) {
        if (buffer.remaining() < Integer.BYTES) return null;
        buffer.mark();
        int size = buffer.getInt();

        if (buffer.remaining() < size) {
            buffer.reset();
            return null;
        }
        byte[] message = new byte[4 + size];
        buffer.reset();
        buffer.get(message);
        return message;
    }
}
