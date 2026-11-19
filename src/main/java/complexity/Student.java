package complexity;

import java.util.Objects;

/** En studerende identificeres af sit id. Navnet er ikke en del af identiteten. */
public final class Student implements Comparable<Student> {
    private final int id;
    private final String name;

    public Student(int id, String name) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name");
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(Student other) {
        // Subtraktion (id - other.id) kan give overflow.
        return Integer.compare(id, other.id);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Student student && id == student.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return id + ": " + name;
    }
}
