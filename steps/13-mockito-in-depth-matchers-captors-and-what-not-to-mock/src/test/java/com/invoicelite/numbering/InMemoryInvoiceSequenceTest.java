package com.invoicelite.numbering;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryInvoiceSequenceTest {

    private static final int THREADS = 8;
    private static final int NUMBERS_PER_THREAD = 500;

    private final InvoiceSequence sequence = new InMemoryInvoiceSequence();

    @Test
    void countsEachYearSeparately() {
        sequence.next(2026);
        sequence.next(2026);

        assertThat(sequence.next(2027)).isEqualTo(1);
        assertThat(sequence.next(2026)).isEqualTo(3);
    }

    @RepeatedTest(value = 20, name = "run {currentRepetition} of {totalRepetitions}")
    void handsOutEveryNumberOnceWhenUsedFromManyThreads() throws InterruptedException {
        Queue<Integer> numbers = new ConcurrentLinkedQueue<>();
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService pool = Executors.newFixedThreadPool(THREADS)) {
            for (int t = 0; t < THREADS; t++) {
                pool.submit(() -> {
                    start.await();
                    for (int i = 0; i < NUMBERS_PER_THREAD; i++) {
                        numbers.add(sequence.next(2026));
                    }
                    return null;
                });
            }
            start.countDown();
        }

        long handedOut = numbers.size();
        long distinct = numbers.stream().distinct().count();
        assertThat(distinct).as("distinct numbers out of %d handed out", handedOut)
                .isEqualTo(THREADS * NUMBERS_PER_THREAD);
    }
}
