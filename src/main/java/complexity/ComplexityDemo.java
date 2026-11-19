package complexity;

import java.io.IOException;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import static complexity.Benchmark.require;

public final class ComplexityDemo {
    private static final int ELEMENTS = 500_000;
    private static final int MIDDLE = 250_000;
    private static final int INDEX_REPETITIONS = 200;
    private static final int FRONT_REPETITIONS = 10_000;
    private static final int SEARCH_REPETITIONS = 1_000;
    private static final int STUDENTS = 100_000;

    private ComplexityDemo() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            throw new IllegalArgumentException("Brug: ./run.sh [sti-til-resultater.csv]");
        }
        Path csv = Path.of(args.length == 1 ? args[0] : "results/measurements.csv");
        System.out.println("Portfolio: Kompleksitet og datastrukturer");
        System.out.println("Tidspunkt: " + ZonedDateTime.now(ZoneId.of("Europe/Copenhagen")));
        System.out.println("Java: " + System.getProperty("java.runtime.version"));
        System.out.println("JVM: " + System.getProperty("java.vm.name"));
        System.out.println("System: " + System.getProperty("os.name") + " "
                + System.getProperty("os.version") + " / " + System.getProperty("os.arch"));
        System.out.printf("Opvarmning: %d runder; måling: %d runder; tabeller viser median.%n",
                Benchmark.WARMUP_ROUNDS, Benchmark.MEASURED_ROUNDS);
        System.out.println("ms/batch er hele gentagelsen; ns/op er batchtid / antal operationer.");

        List<Integer> values = new ArrayList<>(ELEMENTS);
        for (int i = 0; i < ELEMENTS; i++) {
            values.add(i);
        }
        List<Benchmark.Measurement> measurements = new ArrayList<>();
        benchmarkLists(values, measurements);
        benchmarkSearch(values, measurements);
        demonstrateStudentSets();
        benchmarkStudentSets(measurements);
        demonstrateQueues();
        Benchmark.writeCsv(csv, measurements);
        System.out.println("\nAlle fem målinger pr. række er gemt i " + csv);
        System.out.println("Alle kontroller af størrelser og resultater bestod.");
    }

    private static void printHeading(String title) {
        System.out.println("\n" + title);
        System.out.printf("%-24s %-12s %10s %13s %15s%n",
                "Operation", "Struktur", "Antal", "ms/batch", "ns/op");
    }

    private static void benchmarkLists(List<Integer> values,
                                       List<Benchmark.Measurement> measurements) {
        printHeading("1. ArrayList vs LinkedList: 500.000 elementer ved start af hver runde");
        Supplier<List<Integer>> arrayList = () -> {
            // Reservér plads uden for målingen, så vi måler flytning uden en udvidelse.
            ArrayList<Integer> list = new ArrayList<>(ELEMENTS + FRONT_REPETITIONS);
            list.addAll(values);
            return list;
        };
        Supplier<List<Integer>> linkedList = () -> new LinkedList<>(values);

        for (boolean linked : new boolean[]{false, true}) {
            String name = linked ? "LinkedList" : "ArrayList";
            Supplier<List<Integer>> setup = linked ? linkedList : arrayList;
            measurements.add(Benchmark.measure("lists", name, "get(250000)",
                    ELEMENTS, INDEX_REPETITIONS, setup, list -> {
                        long sum = 0;
                        for (int i = 0; i < INDEX_REPETITIONS; i++) {
                            sum += list.get(MIDDLE);
                        }
                        return sum;
                    }, (list, result) -> require(list.size() == ELEMENTS
                            && result == (long) MIDDLE * INDEX_REPETITIONS,
                            "Forkert get-resultat eller størrelse")));

            measurements.add(Benchmark.measure("lists", name, "add(250000, 99)",
                    ELEMENTS, INDEX_REPETITIONS, setup, list -> {
                        for (int i = 0; i < INDEX_REPETITIONS; i++) {
                            list.add(MIDDLE, 99);
                        }
                        return list.size();
                    }, (list, result) -> require(list.size() == ELEMENTS + INDEX_REPETITIONS
                            && list.get(MIDDLE) == 99
                            && list.get(MIDDLE + INDEX_REPETITIONS) == MIDDLE,
                            "Forkert indsættelse i midten")));

            measurements.add(Benchmark.measure("lists", name, "remove(250000)",
                    ELEMENTS, INDEX_REPETITIONS, setup, list -> {
                        long sum = 0;
                        for (int i = 0; i < INDEX_REPETITIONS; i++) {
                            // int-argumentet vælger remove(index), ikke remove(Object).
                            sum += list.remove(MIDDLE);
                        }
                        return sum;
                    }, (list, result) -> require(list.size() == ELEMENTS - INDEX_REPETITIONS
                            && list.get(MIDDLE) == MIDDLE + INDEX_REPETITIONS
                            && result == (long) INDEX_REPETITIONS * MIDDLE
                            + (long) INDEX_REPETITIONS * (INDEX_REPETITIONS - 1) / 2,
                            "Forkert fjernelse i midten")));

            measurements.add(Benchmark.measure("lists", name, "add(0, 99)",
                    ELEMENTS, FRONT_REPETITIONS, setup, list -> {
                        for (int i = 0; i < FRONT_REPETITIONS; i++) {
                            list.add(0, 99);
                        }
                        return list.size();
                    }, (list, result) -> require(list.size() == ELEMENTS + FRONT_REPETITIONS
                            && list.get(0) == 99 && list.get(FRONT_REPETITIONS) == 0,
                            "Forkert indsættelse forrest")));
        }
    }

    private static void benchmarkSearch(List<Integer> values,
                                        List<Benchmark.Measurement> measurements) {
        printHeading("2. contains: samme værdier fra 0 til 499.999 i begge strukturer");
        for (boolean hash : new boolean[]{false, true}) {
            String name = hash ? "HashSet" : "ArrayList";
            Supplier<Collection<Integer>> setup = hash
                    ? () -> new HashSet<>(values) : () -> new ArrayList<>(values);
            for (int target : new int[]{0, ELEMENTS - 1, -1}) {
                Integer query = target;
                boolean expected = target >= 0;
                measurements.add(Benchmark.measure("search", name, "contains(" + target + ")",
                        ELEMENTS, SEARCH_REPETITIONS, setup, collection -> {
                            long found = 0;
                            for (int i = 0; i < SEARCH_REPETITIONS; i++) {
                                found += collection.contains(query) ? 1 : 0;
                            }
                            return found;
                        }, (collection, result) -> require(collection.size() == ELEMENTS
                                        && result == (expected ? SEARCH_REPETITIONS : 0),
                                "Forkert contains-resultat eller størrelse")));
            }
        }
    }

    private static void demonstrateStudentSets() {
        System.out.println("\n3. Student: samme id betyder samme entitet, også med et andet navn");
        demonstrateSet("TreeSet", new TreeSet<>());
        demonstrateSet("HashSet", new HashSet<>());
    }

    private static void demonstrateSet(String name, Set<Student> students) {
        students.add(new Student(42, "Anna"));
        students.add(new Student(7, "Bo"));
        students.add(new Student(18, "Clara"));
        System.out.println(name + " iteration: " + students);
        boolean duplicate = students.add(new Student(7, "Et andet navn"));
        boolean found = students.contains(new Student(18, "Opslag"));
        boolean removed = students.remove(new Student(18, "Fjernelse"));
        System.out.printf("  Tilføj dublet: %s; find id 18: %s; fjern id 18: %s; størrelse: %d%n",
                duplicate, found, removed, students.size());
        require(!duplicate && found && removed && students.size() == 2,
                name + " håndterede ikke identitet korrekt");
    }

    private static void benchmarkStudentSets(List<Benchmark.Measurement> measurements) {
        List<Student> students = new ArrayList<>(STUDENTS);
        List<Student> probes = new ArrayList<>(STUDENTS);
        for (int id = 0; id < STUDENTS; id++) {
            students.add(new Student(id, "Student " + id));
            // Nye instanser bekræfter, at vi finder værdier frem for referencer.
            probes.add(new Student(id, "Opslag"));
        }
        Collections.shuffle(students, new Random(42));
        Collections.shuffle(probes, new Random(84));
        printHeading("4. TreeSet vs HashSet: 100.000 forskellige Student-objekter");

        for (boolean hash : new boolean[]{false, true}) {
            String name = hash ? "HashSet" : "TreeSet";
            Supplier<Set<Student>> empty = hash ? HashSet::new : TreeSet::new;
            Supplier<Set<Student>> populated = () -> {
                Set<Student> set = empty.get();
                set.addAll(students);
                return set;
            };
            measurements.add(Benchmark.measure("students", name, "add (alle)",
                    STUDENTS, STUDENTS, empty, set -> {
                        long added = 0;
                        for (Student student : students) {
                            added += set.add(student) ? 1 : 0;
                        }
                        return added;
                    }, (set, result) -> require(set.size() == STUDENTS && result == STUDENTS,
                            "Ikke alle studerende blev tilføjet")));

            measurements.add(Benchmark.measure("students", name, "contains (alle)",
                    STUDENTS, STUDENTS, populated, set -> {
                        long found = 0;
                        for (Student probe : probes) {
                            found += set.contains(probe) ? 1 : 0;
                        }
                        return found;
                    }, (set, result) -> require(set.size() == STUDENTS && result == STUDENTS,
                            "Forkert contains-resultat eller størrelse")));

            measurements.add(Benchmark.measure("students", name, "remove (alle)",
                    STUDENTS, STUDENTS, populated, set -> {
                        long removed = 0;
                        for (Student probe : probes) {
                            removed += set.remove(probe) ? 1 : 0;
                        }
                        return removed;
                    }, (set, result) -> require(set.isEmpty() && result == STUDENTS,
                            "Ikke alle studerende blev fjernet")));
        }
    }

    private static void demonstrateQueues() {
        System.out.println("\n5. PriorityQueue og ArrayDeque: indsæt 30, 10, 20");
        Queue<Integer> priority = new PriorityQueue<>();
        Queue<Integer> fifo = new ArrayDeque<>();
        ArrayDeque<Integer> stack = new ArrayDeque<>();
        for (int value : new int[]{30, 10, 20}) {
            priority.offer(value);
            fifo.offer(value);
            stack.push(value);
        }
        System.out.println("PriorityQueue poll(): " + drain(priority));
        System.out.println("ArrayDeque som kø poll(): " + drain(fifo));
        List<Integer> stackOrder = new ArrayList<>();
        while (!stack.isEmpty()) {
            stackOrder.add(stack.pop());
        }
        System.out.println("ArrayDeque som stak pop(): " + stackOrder);
    }

    private static List<Integer> drain(Queue<Integer> queue) {
        List<Integer> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            result.add(queue.poll());
        }
        return result;
    }
}
