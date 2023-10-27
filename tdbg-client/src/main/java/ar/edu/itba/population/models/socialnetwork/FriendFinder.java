package ar.edu.itba.population.models.socialnetwork;

import ar.edu.itba.util.Utils;

import java.util.*;
import java.util.stream.Collectors;

public class FriendFinder {

    private final List<Person> persons;
    private final int maxFriendships;

    public FriendFinder(List<Person> persons, int maxFriendships){
        this.maxFriendships = maxFriendships;
        this.persons = persons;
    }

    public List<Person> getFriendsFor(Person person) {
        int currentFriends = person.numberOfFriends();
        if (maxFriendships <= currentFriends) {
            return new ArrayList<>();
        }

        int numberOfNewFriends = Utils.randomInteger(1, maxFriendships - currentFriends + 1);
        Set<Integer> newFriendsIndexes = indexes(numberOfNewFriends);
        List<Person> potentialFriends = newFriendsIndexes.parallelStream().map(persons::get).collect(Collectors.toList());
        return potentialFriends.parallelStream().filter(p -> !person.isFriendsWith(p))
                .filter(p -> p.getId() != person.getId())
                .collect(Collectors.toList());
    }

    private Set<Integer> indexes(int numberOfNewFriends) {
        Set<Integer> indexes = new HashSet<>();
        for (int i = 0; i < numberOfNewFriends; i++) {
            int index = Utils.randomInteger(0, persons.size());
            indexes.add(index);
        }
        return indexes;
    }
}
