package complexity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Kan køres uden testbiblioteker: ./run.sh --check */
public final class StudentChecks {
    private static int checks;

    private StudentChecks() {
    }

    public static void main(String[] args) {
        Student first = new Student(7, "Bo");
        Student sameId = new Student(7, "Et andet navn");
        Student third = new Student(7, "Tredje navn");
        check(first.equals(first), "equals skal være refleksiv");
        check(first.equals(sameId) && sameId.equals(first), "equals skal være symmetrisk");
        check(first.equals(sameId) && sameId.equals(third) && first.equals(third),
                "equals skal være transitiv");
        check(!first.equals(null) && !first.equals("7"), "Forkert type/null må ikke være ens");
        check(!first.equals(new Student(8, "Bo")), "Navn alene må ikke definere identitet");
        check(first.hashCode() == sameId.hashCode(), "Ens objekter skal have samme hash");
        check(first.compareTo(sameId) == 0, "compareTo skal stemme med equals");

        List<Student> values = List.of(new Student(Integer.MIN_VALUE, "Min"),
                new Student(-1, "Minus"), new Student(0, "Nul"),
                new Student(7, "Bo"), sameId, new Student(Integer.MAX_VALUE, "Max"));
        for (Student left : values) {
            for (Student right : values) {
                check((left.compareTo(right) == 0) == left.equals(right),
                        "Sortering og identitet er uenige");
                check(Integer.signum(left.compareTo(right))
                                == -Integer.signum(right.compareTo(left)),
                        "Sortering skal vende fortegn, også ved ekstreme id'er");
                for (Student last : values) {
                    if (left.compareTo(right) < 0 && right.compareTo(last) < 0) {
                        check(left.compareTo(last) < 0, "Sortering skal være transitiv");
                    }
                }
            }
        }

        checkSet(new TreeSet<>());
        checkSet(new HashSet<>());
        System.out.println(checks + " kontroller bestod: identitet, hash, sortering og begge sæt.");
    }

    private static void checkSet(Set<Student> set) {
        check(set.add(new Student(7, "Bo")), "Første id skal kunne tilføjes");
        check(!set.add(new Student(7, "Nyt navn")), "Samme id må ikke give en dublet");
        check(set.add(new Student(8, "Bo")), "Samme navn med nyt id skal kunne tilføjes");
        check(set.contains(new Student(7, "Opslag")), "Ny instans med samme id skal findes");
        check(set.remove(new Student(7, "Fjern")), "Ny instans med samme id skal kunne fjernes");
        check(!set.contains(new Student(7, "Opslag")), "Fjernet id må ikke findes");
        check(!set.remove(new Student(7, "Fjern igen")), "Id må kun fjernes én gang");
        check(set.size() == 1, "Kun id 8 skal være tilbage");
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
