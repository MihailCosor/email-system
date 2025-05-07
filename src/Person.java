import java.io.Serializable;

public abstract class Person implements Serializable {
    protected static int nextId = 1;
    protected int id;
    protected String name;
    protected String email;

    public Person(String name, String email) {
        this.id = nextId++;
        this.name = name;
        this.email = email;
    }

    public Person(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        nextId = Math.max(nextId, id + 1);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    public void setName(String name) { this.name = name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return id == person.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
} 