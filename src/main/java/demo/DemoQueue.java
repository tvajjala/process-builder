package demo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DemoQueue {


    public static void main(String[] args) {

        VersionedQueue<MessageWithTTL<ClusterSummary>> summary=new VersionedQueue<>();


        for (int i = 0; i < 10; i++) {
            summary.offer(new MessageWithTTL<>(new ClusterSummary(String.valueOf(i), i),0L));
        }

        for (int i = 0; i < 10; i++) {
            summary.offer(new MessageWithTTL<>(new ClusterSummary(String.valueOf(i), i),0L));
        }

        System.out.println(summary.getLength());


    }
}



@AllArgsConstructor
@Getter
class ClusterSummary  {

    private String name;
    private int id;
}



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
 class MessageWithTTL<T> {
    /** The message content of generic type T */
    private T message;

    /** Time in milliseconds when the message should expire */
    private long expirationTime;

    /**
     * Constructs a new instance by initializing the message content and setting the time at which
     * it will expire based on provided TTL value and time unit.
     *
     * @param message The message content of generic type T
     * @param ttl The time-to-live duration
     * @param unit The time unit associated with the given TTL value
     */
    public MessageWithTTL(T message, long ttl, TimeUnit unit) {
        this.message = message;
        this.expirationTime = System.currentTimeMillis() + unit.toMillis(ttl);
    }

    /**
     * Calculates the remaining delay until the message expires.
     *
     * @param unit The desired time unit for the returned delay
     * @return Remaining delay until the message expires in specified time unit
     */
    public long getDelay(TimeUnit unit) {
        long delay = expirationTime - System.currentTimeMillis();
        return unit.convert(delay, TimeUnit.MILLISECONDS);
    }
}

class VersionedQueue<T> {
    private final ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger version = new AtomicInteger(0);

    public void offer(T item) {
        queue.offer(item);
    }

    public T poll() {
        return queue.poll();
    }

    public void clear() {
        queue.clear();
        version.incrementAndGet(); // Increase the version after clearing
    }

    public int getVersion() {
        return version.get();
    }

    public int getLength(){
        return queue.size();
    }
}