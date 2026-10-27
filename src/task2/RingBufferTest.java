package task2;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RingBufferTest {

    @Test
    public void keepsFifoOrderAndWrapsAround() throws InterruptedException {
        RingBuffer<Integer> buffer = new RingBuffer<>(3);
        int next = 0;
        int expected = 0;
        for (int round = 0; round < 10; round++) {
            buffer.put(next++);
            buffer.put(next++);
            assertEquals(expected++, (int) buffer.take());
            assertEquals(expected++, (int) buffer.take());
        }
    }

    @Test
    public void canHoldExactlyCapacityElements() throws InterruptedException {
        RingBuffer<Integer> buffer = new RingBuffer<>(4);
        for (int i = 0; i < 4; i++) {
            buffer.put(i);
        }
        for (int i = 0; i < 4; i++) {
            assertEquals(i, (int) buffer.take());
        }
    }

    @Test(timeout = 10_000)
    public void takeWaitsWhileBufferIsEmpty() throws InterruptedException {
        RingBuffer<String> buffer = new RingBuffer<>(2);
        AtomicBoolean got = new AtomicBoolean();
        Thread consumer = new Thread(() -> {
            try {
                buffer.take();
                got.set(true);
            } catch (InterruptedException ignored) {
            }
        });
        consumer.start();
        Thread.sleep(200);
        assertFalse(got.get());
        buffer.put("x");
        consumer.join();
        assertTrue(got.get());
    }

    @Test(timeout = 10_000)
    public void putWaitsWhileBufferIsFull() throws InterruptedException {
        RingBuffer<Integer> buffer = new RingBuffer<>(2);
        buffer.put(1);
        buffer.put(2);
        AtomicBoolean added = new AtomicBoolean();
        Thread producer = new Thread(() -> {
            try {
                buffer.put(3);
                added.set(true);
            } catch (InterruptedException ignored) {
            }
        });
        producer.start();
        Thread.sleep(200);
        assertFalse(added.get());
        assertEquals(1, (int) buffer.take());
        producer.join();
        assertTrue(added.get());
    }

    @Test(timeout = 30_000)
    public void manyProducersAndConsumersLoseNothing() throws InterruptedException {
        RingBuffer<Integer> buffer = new RingBuffer<>(5);
        int producers = 5;
        int perProducer = 20_000;
        List<Integer> taken = Collections.synchronizedList(new ArrayList<>());
        List<Thread> threads = new ArrayList<>();

        for (int p = 0; p < producers; p++) {
            int base = p * perProducer;
            Thread t = new Thread(() -> {
                try {
                    for (int i = 0; i < perProducer; i++) {
                        buffer.put(base + i);
                    }
                } catch (InterruptedException ignored) {
                }
            });
            threads.add(t);
        }
        for (int c = 0; c < 3; c++) {
            int share = producers * perProducer / 3 + (c == 0 ? producers * perProducer % 3 : 0);
            Thread t = new Thread(() -> {
                try {
                    for (int i = 0; i < share; i++) {
                        taken.add(buffer.take());
                    }
                } catch (InterruptedException ignored) {
                }
            });
            threads.add(t);
        }
        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }

        assertEquals(producers * perProducer, taken.size());
        boolean[] seen = new boolean[producers * perProducer];
        for (int value : taken) {
            assertFalse("duplicate " + value, seen[value]);
            seen[value] = true;
        }
    }

    @Test(timeout = 20_000)
    public void pipelineProducesHundredCorrectMessages() throws InterruptedException {
        List<String> messages = Pipeline.run(100);
        assertEquals(100, messages.size());
        for (String message : messages) {
            assertTrue(message, message.matches(
                    "Потік № [67] переклав повідомлення «Потік № [1-5] згенерував повідомлення \\d+»"));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroCapacityIsRejected() {
        new RingBuffer<String>(0);
    }
}
