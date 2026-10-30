package com.ocsin.jaya.application.perf;

import com.ocsin.jaya.domain.Clock;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Mesure les cas d'usage et conserve les opérations les plus lentes.
 */
public final class OperationMonitor {

    private final long thresholdNanos;
    private final int capacity;
    private final Clock clock;
    private final Deque<SlowOperation> slow = new ArrayDeque<>();
    private final Map<String, AtomicLong> counts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> durations = new ConcurrentHashMap<>();

    public OperationMonitor(Duration slowThreshold, int capacity, Clock clock) {
        this.thresholdNanos = slowThreshold.toNanos();
        this.capacity = capacity;
        this.clock = clock;
    }

    public <T> T time(String name, Supplier<T> supplier) {
        long start = System.nanoTime();
        try {
            return supplier.get();
        } finally {
            record(name, System.nanoTime() - start);
        }
    }

    public void time(String name, Runnable runnable) {
        time(name, () -> {
            runnable.run();
            return null;
        });
    }

    public List<SlowOperation> slowOperations() {
        synchronized (slow) {
            return List.copyOf(slow);
        }
    }

    public List<OperationStats> operations() {
        List<OperationStats> stats = new ArrayList<>();
        for (Map.Entry<String, AtomicLong> entry : counts.entrySet()) {
            long count = entry.getValue().get();
            long totalNanos = durations.getOrDefault(entry.getKey(), new AtomicLong()).get();
            long average = count == 0 ? 0 : (totalNanos / count) / 1_000_000L;
            stats.add(new OperationStats(entry.getKey(), count, average));
        }
        stats.sort(Comparator.comparing(OperationStats::getName));
        return List.copyOf(stats);
    }

    private void record(String name, long elapsedNanos) {
        counts.computeIfAbsent(name, ignored -> new AtomicLong()).incrementAndGet();
        durations.computeIfAbsent(name, ignored -> new AtomicLong()).addAndGet(elapsedNanos);
        if (elapsedNanos < thresholdNanos) {
            return;
        }
        synchronized (slow) {
            slow.addFirst(new SlowOperation(name, elapsedNanos / 1_000_000L, clock.now()));
            while (slow.size() > capacity) {
                slow.removeLast();
            }
        }
    }
}
