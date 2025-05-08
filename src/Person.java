import java.io.Serializable;

// base class for all person entities in the system (users, contacts, etc.)
public abstract class Person implements Serializable {
    // auto-incrementing counter for generating unique person ids
    protected static int nextId = 1;
    // unique identifier for each person
    protected int id;
    // full name of the person
    protected String name;
    // email address used as username/identifier
    protected String email;

    // creates a new person with auto-generated id
    public Person(String name, String email) {
        this.id = nextId++;
        this.name = name;
        this.email = email;
    }

    // creates a person with a specific id (used for data loading)
    public Person(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        // ensure nextId stays ahead of manually set ids
        nextId = Math.max(nextId, id + 1);
    }

    // retrieves the person's unique identifier
    public int getId() { return id; }
    // retrieves the person's full name
    public String getName() { return name; }
    // retrieves the person's email address
    public String getEmail() { return email; }

    // updates the person's name
    public void setName(String name) { this.name = name; }

    // implements equality based on unique id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return id == person.id;
    }

    // uses id as hash code for consistent hashing
    @Override
    public int hashCode() {
        return id;
    }
} 