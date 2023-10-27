package ar.edu.itba.population.models.socialnetwork;

import ar.edu.itba.util.interval.Interval;
import ar.edu.itba.population.models.ObjectNode;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Person extends ObjectNode {

    public static final String TITLE = "Person";
    public Set<Person> friends = new HashSet<>();

    public Person(int id, Interval interval) {
        super(id, interval);
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    public void addFriend(Person friend) {
        friends.add(friend);
    }

    public int numberOfFriends() {
        return friends.size();
    }

    public boolean isFriendsWith(Person person) {
        return friends.contains(person);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(this.getId(), person.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
