import java.util.Objects;

public class Event {
    public final long timestamp;
    public final long orderId;
    public final double price;
    public final int quantity;
    public final byte eventType;
    public final byte side; //0=BUY, 1=SELL;

    public Event(long timestamp, long orderId, double price, int quantity, byte eventType, byte side) {
        this.timestamp = timestamp;
        this.orderId = orderId;
        this.price = price;
        this.quantity = quantity;
        this.eventType = eventType;
        this.side = side;
    }

    @Override
    public String toString() {
        return String.format("Event{ts=%d, type=%d, orderId=%d, qty=%d, price=%.2f, side=%d}", timestamp, eventType, orderId, quantity, price, side);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event e)) return false;

        return timestamp == e.timestamp && orderId == e.orderId && Double.compare(price, e.price) == 0 && quantity == e.quantity && eventType == e.eventType && side == e.side;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, orderId, price, quantity, eventType, side);
    }


}
