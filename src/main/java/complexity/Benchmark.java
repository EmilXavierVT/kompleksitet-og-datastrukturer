package complexity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;

/** Lille undervisningsbenchmark: opsætning og kontrol ligger uden for tidsmålingen. */
final class Benchmark {
    static final int WARMUP_ROUNDS = 3;
    static final int MEASURED_ROUNDS = 5;

    // Resultatet bruges uden for tidsmålingen, så arbejdet har en observerbar effekt.
    // Det fjerner ikke alle de optimeringer, en JVM kan lave.
    private static volatile long sink;

    private Benchmark() {
    }

    record Measurement(String section, String structure, String operation,
                       int elements, int repetitions, long[] samples) {
        long medianNanos() {
            long[] sorted = samples.clone();
            Arrays.sort(sorted);
            return sorted[sorted.length / 2];
        }

        void print() {
            System.out.printf(Locale.ROOT, "%-24s %-12s %10d %13.3f %15.1f%n",
                    operation, structure, repetitions, medianNanos() / 1_000_000.0,
                    medianNanos() / (double) repetitions);
        }
    }

    static <T> Measurement measure(String section, String structure, String operation,
                                   int elements, int repetitions, Supplier<T> setup,
                                   ToLongFunction<T> action, ObjLongConsumer<T> verify) {
        long[] samples = new long[MEASURED_ROUNDS];
        for (int round = 0; round < WARMUP_ROUNDS + MEASURED_ROUNDS; round++) {
            T state = setup.get();
            long start = System.nanoTime();
            long result = action.applyAsLong(state);
            long elapsed = System.nanoTime() - start;
            sink = result;
            verify.accept(state, result);
            if (round >= WARMUP_ROUNDS) {
                samples[round - WARMUP_ROUNDS] = elapsed;
            }
        }
        Measurement measurement = new Measurement(section, structure, operation,
                elements, repetitions, samples);
        measurement.print();
        return measurement;
    }

    static void writeCsv(Path output, List<Measurement> measurements) throws IOException {
        StringBuilder csv = new StringBuilder(
                "section,structure,operation,elements,operations,sample,ns_total,ns_per_operation\n");
        for (Measurement measurement : measurements) {
            for (int sample = 0; sample < measurement.samples().length; sample++) {
                long nanos = measurement.samples()[sample];
                csv.append(String.format(Locale.ROOT, "%s,%s,%s,%d,%d,%d,%d,%.3f%n",
                        quote(measurement.section()), quote(measurement.structure()),
                        quote(measurement.operation()), measurement.elements(),
                        measurement.repetitions(), sample + 1, nanos,
                        nanos / (double) measurement.repetitions()));
            }
        }
        Files.createDirectories(output.toAbsolutePath().getParent());
        Files.writeString(output, csv, StandardCharsets.UTF_8);
    }

    private static String quote(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
