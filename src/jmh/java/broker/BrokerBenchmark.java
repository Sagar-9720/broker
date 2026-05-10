package broker;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmarks for broker
 * Measures throughput (msgs/sec) and latency (p50, p99, max)
 */
@State(Scope.Benchmark)
@Fork(1)
@Measurement(iterations = 3, time = 10)
@Warmup(iterations = 2, time = 5)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class BrokerBenchmark {

    private static final int NUM_PRODUCERS = 3;
    private static final int EVENTS_PER_PRODUCER = 10000;
    private EventQueue eventQueue;
    private ExecutorService executorService;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BrokerBenchmark.class.getName())
                .result("jmh-results.json")
                .resultFormat(ResultFormatType.JSON)
                .build();

        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void setup() {
        eventQueue = new EventQueue();
        executorService = Executors.newFixedThreadPool(NUM_PRODUCERS + 1);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        executorService.shutdown();
    }

    /**
     * Benchmark: Single producer, single consumer
     */
    @Benchmark
    public void singleProducerSingleConsumer() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(NUM_PRODUCERS);

        // Producer tasks
        for (int p = 0; p < NUM_PRODUCERS; p++) {
            executorService.submit(() -> {
                for (int i = 0; i < EVENTS_PER_PRODUCER; i++) {
                    Event event = new Event(System.nanoTime(), i, 1000.0, 100, (byte) 0, (byte) 0);
                    eventQueue.enqueue(event);
                }
                latch.countDown();
            });
        }

        // Consumer task
        executorService.submit(() -> {
            long consumed = 0;
            while (consumed < EVENTS_PER_PRODUCER * NUM_PRODUCERS) {
                Event e = eventQueue.dequeue();
                if (e != null) consumed++;
            }
        });

        latch.await();
    }

    /**
     * Benchmark: Latency measurement (nanoseconds)
     */
    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void enqueueLatency() {
        Event event = new Event(System.nanoTime(), 123L, 1000.0, 100, (byte) 0, (byte) 0);
        eventQueue.enqueue(event);
    }
}

