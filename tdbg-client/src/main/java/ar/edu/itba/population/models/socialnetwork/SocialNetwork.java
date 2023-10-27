package ar.edu.itba.population.models.socialnetwork;

import ar.edu.itba.population.Population;
import ar.edu.itba.population.TimeInterval;
import ar.edu.itba.population.TimeIntervalListWithStats;
import ar.edu.itba.population.models.*;
import ar.edu.itba.population.models.common.City;
import ar.edu.itba.util.Pair;
import ar.edu.itba.population.models.cpath.CPathGenerationConfiguration;
import ar.edu.itba.population.models.cpath.CPathGenerator;
import ar.edu.itba.population.models.cpath.CPathIntervalGenerator;
import ar.edu.itba.util.Utils;
import ar.edu.itba.util.interval.Granularity;
import ar.edu.itba.util.interval.Interval;
import ar.edu.itba.util.interval.IntervalListWithStats;
import ar.edu.itba.util.interval.IntervalSet;

import org.neo4j.driver.internal.shaded.io.netty.util.internal.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class SocialNetwork extends Population {

    private final Logger logger = LoggerFactory.getLogger(SocialNetwork.class);

    /**
     * Number of persons
     */
    private final int personCount;

    /**
     * Maximum amount of friendships for each person
     */
    private final int maxFriendships;

    /**
     * Maximum amount of intervals on each friendship
     */
    private final int maxFriendshipIntervals;

    /**
     * Number of brands
     */
    private final int brandCount;

    /**
     * Maximum amount of fans for each brand
     */
    private final int maxFans;

    private final List<CPathGenerationConfiguration> cPathGenerationConfigurations;

    /**
     * Maximum amount of intervals on each fan relationship
     */
    private final int maxFansIntervals;

    private final int cityCount;

    private final int starCount;

    private final int minStarFollowerCount;

    private final Map<Integer, Person> persons = new HashMap<>();

    private final Map<Integer, Person> persons2 = new HashMap<>();

    private final Map<Integer, Person> stars = new HashMap<>();

    private final Map<Integer, Brand> brands = new HashMap<>();

    private final Map<Integer, City> cities = new HashMap<>();

    private final List<Relationship> livedIn = new ArrayList<>();

    private final List<Relationship> friendships = new ArrayList<>();

    private final List<Relationship> fans = new ArrayList<>();

    private Long maxPersonStartSeconds = 0L;

    private final HashMap<Pair<Integer, Integer>, Relationship> friendshipsMap = new HashMap<>();

    protected SocialNetwork(SocialNetworkBuilder builder) {
        this.personCount = builder.personCount;
        this.maxFriendships = builder.maxFriendships;
        this.maxFriendshipIntervals = builder.maxFriendshipIntervals;
        this.brandCount = builder.brandCount;
        this.maxFans = builder.maxFans;
        this.maxFansIntervals = builder.maxFansIntervals;
        this.cityCount = builder.cityCount;
        this.starCount = builder.starCount;
        this.minStarFollowerCount = builder.minStarFollowerCount;
        this.cPathGenerationConfigurations = builder.cPathConfigurations;
    }

    public static int generatedNodes(List<CPathGenerationConfiguration> configurations) {
        return configurations.stream()
                .map(CPathGenerationConfiguration::nodesToGenerate)
                .reduce(0, Integer::sum);
    }

    protected void generateNodesAndRelationships() {

        // logger.debug("Creating cities...");
        // for (int i = 0; i < cityCount; i++){
        //     createCity();
        // }

        cPathGenerationConfigurations.forEach(this::generateCPaths);

        int remainingPersons = personCount - generatedNodes(this.cPathGenerationConfigurations) - starCount;

        logger.debug("Creating persons...");
        for (int i = 0; i < remainingPersons; i++) {
            final Interval t = new Interval(Interval.MAX_SECONDS_FROM_EPOCH, Granularity.DATE);
            // if (maxPersonStartSeconds < t.getStart()) {
            //     maxPersonStartSeconds = t.getStart();
            // }
            createPerson(t);
        }

        // logger.debug("Creating brands...");
        // for (int i = 0; i < brandCount; i++) {
        //     createBrand();
        // }

        
        // logger.debug("Creating lived in edges...");
        // persons.keySet().forEach(this::createLivedIn);
        
        logger.debug("Creating friendship edges...");
        List<Person> personList = new ArrayList<>(persons.values());
        FriendFinder friendFinder = new FriendFinder(personList, maxFriendships);
        for (Person person : personList) {
            createFriendships(person, friendFinder);
        }

        logger.debug("Creating STARS...");
        createStars();
        
        // logger.debug("Creating fan edges...");
        // brands.values().forEach(this::createFans);
        generateNodesAndRelationshipsForSmallComponent();
        joinComponents();
        unwindData();
    }

    protected void joinComponents() {
        List<Integer> smallComponentIds = new ArrayList<>(persons2.keySet());
        List<Integer> bigComponentIds = new ArrayList<>(persons.keySet());
        Collections.shuffle(bigComponentIds);
        for (int i = 0; i < smallComponentIds.size() && i < bigComponentIds.size(); i++) {
            Person p1 = persons2.get(smallComponentIds.get(i));
            Person p2 = persons.get(bigComponentIds.get(i));
            Integer th = ThreadLocalRandom.current().nextInt(0, 2);
            Interval interval = Interval.randomInterval((Long) Math.max(p1.getInterval().getStart(), p2.getInterval().getStart()), Interval.MAX_SECONDS_FROM_EPOCH);
            List<Interval> intervalCollection = Collections.singletonList(interval);
            if (th < 1) {
                createFriendship(p1, p2, intervalCollection);
            } else {
                createFriendship(p2, p1, intervalCollection);
            }
        }
    }

    protected void generateNodesAndRelationshipsForSmallComponent() {

        List<CPathGenerationConfiguration> configs = cPathGenerationConfigurations.stream()
            .map(cPathConfig ->
                new CPathGenerationConfiguration(cPathConfig.getNumberOfPaths() * 10 / 100, cPathConfig.getMinimumLengthOfPath())).collect(Collectors.toList());

        configs.forEach(this::generateMildCPaths);

        int remainingPersons = personCount / 10 - generatedNodes(configs);

        logger.debug("Creating persons...");
        for (int i = 0; i < remainingPersons; i++) {
            final Interval t = new Interval(Interval.MAX_SECONDS_FROM_EPOCH, Granularity.DATE);
            // if (maxPersonStartSeconds < t.getStart()) {
            //     maxPersonStartSeconds = t.getStart();
            // }
            createPerson2(t);
        }
        
        logger.debug("Creating friendship edges...");
        List<Person> personList = new ArrayList<>(persons2.values());
        FriendFinder friendFinder = new FriendFinder(personList, maxFriendships / 10);
        for (Person person : personList) {
            createFriendships(person, friendFinder);
        }
    }

    private void unwindData() {
        logger.debug("Starting unwind process");
        logger.debug("Unwinding persons...");
        unwindObjectNodes(persons);
        logger.debug("Unwinding persons2...");
        unwindObjectNodes(persons2);
        logger.debug("Unwinding brands...");
        unwindObjectNodes(brands);
        logger.debug("Unwinding cities...");
        unwindObjectNodes(cities);
        logger.debug("Unwinding attribute and value nodes...");
        unwindAttributesAndValues();
        logger.debug("Unwinding lived in...");
        unwindRelationships(livedIn, RelationshipType.LIVED_IN);
        logger.debug("Unwinding friendships...");
        unwindFriendships();
        logger.debug("Unwinding fans...");
        unwindRelationships(fans, RelationshipType.FAN);
    }

    private void unwindFriendships() {
        unwindRelationships(friendships, RelationshipType.FRIEND);
    }

    private void unwindObjectNodes(Map<Integer, ? extends ObjectNode> objectNodes) {
        List<ObjectNode> objectNodeList = new ArrayList<>(objectNodes.values());
        unwindNodes(objectNodeList, ObjectNode.LABEL);
    }

    private void generateMildCPaths(CPathGenerationConfiguration cPathGenerationConfiguration) {
        for (int path = 0; path < cPathGenerationConfiguration.getNumberOfPaths(); path++) {
            int numberOfPersons = cPathGenerationConfiguration.getMinimumLengthOfPath() + 1;

            IntervalListWithStats timeIntervalListWithStats = Interval.list(numberOfPersons);
            List<Interval> objectNodeIntervals = timeIntervalListWithStats.getIntervals();

            Pair<List<Interval>, Interval> cPathIntervals = new CPathGenerator(new CPathIntervalGenerator())
                    .generate(timeIntervalListWithStats);

            List<Interval> relationShipIntervals = cPathIntervals.getLeft();

            List<Integer> ids = new ArrayList<>();

            for (Interval nodeInterval : objectNodeIntervals) {
                Person person = createPerson2(nodeInterval);
                ids.add(person.getId());
            }

            Integer source = ids.get(0);
            Integer destination = ids.get(ids.size()-1);

            for (int i = 0; i < relationShipIntervals.size(); i++) {
                Person firstPerson = persons2.get(ids.get(i));
                Person secondPerson = persons2.get(ids.get(i + 1));
                createFriendship(firstPerson, secondPerson, Collections.singletonList(relationShipIntervals.get(i)));
            }
            logger.info("CPath of length {} with interval {} generated from node {} to {}. Ids {}, intervals {}", cPathGenerationConfiguration.getMinimumLengthOfPath(), cPathIntervals.getRight(), ids.get(0), ids.get(ids.size() - 1), ids, relationShipIntervals);

        }
    }

    private void generateCPaths(CPathGenerationConfiguration cPathGenerationConfiguration) {
        for (int path = 0; path < cPathGenerationConfiguration.getNumberOfPaths(); path++) {
            int numberOfPersons = cPathGenerationConfiguration.getMinimumLengthOfPath() + 1;

            IntervalListWithStats timeIntervalListWithStats = Interval.list(numberOfPersons);
            List<Interval> objectNodeIntervals = timeIntervalListWithStats.getIntervals();

            Pair<List<Interval>, Interval> cPathIntervals = new CPathGenerator(new CPathIntervalGenerator())
                    .generate(timeIntervalListWithStats);

            List<Interval> relationShipIntervals = cPathIntervals.getLeft();

            List<Integer> ids = new ArrayList<>();

            for (Interval nodeInterval : objectNodeIntervals) {
                Person person = createPerson(nodeInterval);
                ids.add(person.getId());
            }

            Integer source = ids.get(0);
            Integer destination = ids.get(ids.size()-1);

            for (int i = 0; i < relationShipIntervals.size(); i++) {
                Person firstPerson = persons.get(ids.get(i));
                Person secondPerson = persons.get(ids.get(i + 1));
                createFriendship(firstPerson, secondPerson, Collections.singletonList(relationShipIntervals.get(i)));
            }

            Random rd = new Random();
            for (int i = 0; i < 15; i++) {
                List<Integer> personIds = new ArrayList<>(persons.keySet());
                Collections.shuffle(personIds);
                LinkedList<Integer> subIds = new LinkedList<>(personIds.subList(0, ids.size()-2));
                subIds.addFirst(source);
                subIds.add(destination);

                Interval finalInterval = persons.get(subIds.get(0)).getInterval();
                boolean notContinuous = false;
                List<Interval> relIntervals = new ArrayList<>();
                for (Integer id : subIds) {
                    Optional<Interval> aux = finalInterval.intersection(persons.get(id).getInterval());
                    if (!aux.isPresent()) {
                        notContinuous = true;
                        break;
                    }
                    relIntervals.add(Interval.randomInterval(aux.get().getStart(), aux.get().getEnd()));
                }
                if (notContinuous) continue;

                for (int j = 0; j < subIds.size() - 1; j ++) {
                    Person firstPerson = persons.get(subIds.get(j));
                    Person secondPerson = persons.get(subIds.get(j + 1));
                    createFriendship(firstPerson, secondPerson, Collections.singletonList(relIntervals.get(j)));
                }
            }
            if (cPathGenerationConfiguration.getMinimumLengthOfPath() < 5) {
                logger.info("CPath of length {} with interval {} generated from node {} to {}. Ids {}, intervals {}", cPathGenerationConfiguration.getMinimumLengthOfPath(), cPathIntervals.getRight(), ids.get(0), ids.get(ids.size() - 1), ids, relationShipIntervals);
            }
        }
    }

    /**
     * Creates the edge for the relation "LivedIn".
     * @param personId id of the person from which the edge will start.
     */
    private void createLivedIn(int personId) {
        List<Integer> randomCitiesIds = randomCities();
        // List<Interval> intervals = Interval.createRandomConsecutiveIntervals(persons.get(personId).getInterval(), randomCitiesIds.size());

        // for (int i = 0; i < intervals.size(); i++) {
        //     int randomCityId = randomCitiesIds.get(i);
        //     Relationship relationship = new Relationship(RelationshipType.LIVED_IN, persons.get(personId),
        //             cities.get(randomCityId), Collections.singletonList(intervals.get(i)));
        //     livedIn.add(relationship);
        // }
    }

    /**
     * Selects a random amount of cities from the available ones
     * @return a list of cities
     */
    private List<Integer> randomCities() {
        int numberOfCitiesLivedIn = Utils.randomInteger(1, cities.size());
        List<Integer> cityIds = new ArrayList<>(cities.keySet());
        Collections.shuffle(cityIds);
        return cityIds.subList(0, numberOfCitiesLivedIn);
    }

    /**
     * Creates friendship edges for a person
     * @param person person to create friendships
     */
    private void createFriendships(final Person person, FriendFinder friendFinder) {
        final List<Person> randomFriends = friendFinder.getFriendsFor(person);

        for (Person friend : randomFriends) {
            final Optional<Interval> timeInterval = friend.getInterval().intersection(person.getInterval());

            if (timeInterval.isPresent()) {
                int totalNumberOfIntervals = Utils.randomInteger(1, maxFriendshipIntervals + 1);
                List<Interval> intervalList = Interval.createRandomDisjointIntervals(timeInterval.get(), totalNumberOfIntervals);
                createFriendship(person, friend, intervalList);
            } else {
                logger.error("Error creating friendship edge: empty interval. Skipping.");
            }
        }
    }

    private void createFriendship(Person firstPerson, Person secondPerson, List<Interval> intervals) {
        if (firstPerson.getId() == secondPerson.getId())
            return;
        Relationship friendshipEdge = new Relationship(RelationshipType.FRIEND, firstPerson, secondPerson, intervals);
        Pair<Integer, Integer> pairKey = new Pair<>(firstPerson.getId(), secondPerson.getId());
        // logger.info("Creating rel for {}", pairKey.toString());
        if (friendshipsMap.containsKey(pairKey)) {
            IntervalSet existingIntervals = new IntervalSet(friendshipsMap.get(pairKey).getIntervals());
            for (Interval i : intervals) {
                if (existingIntervals.intersection(i).isEmpty())
                    existingIntervals = existingIntervals.union(i);
                else {
                    System.out.println("generated interval intersects with existing ones");
                }
            }
            friendshipsMap.get(pairKey).setIntervals(existingIntervals.getIntervals());
        } else {
            firstPerson.addFriend(secondPerson);
            secondPerson.addFriend(firstPerson);
            friendships.add(friendshipEdge);
            friendshipsMap.put(pairKey, friendshipEdge);
        }
    }

    /**
     * Creates fan edges for a brand
     * @param brand brand to create friendships
     */
    private void createFans(Brand brand) {
        final List<Integer> randomFans = randomFans();

        for (Integer fanId : randomFans) {
            Person fan = persons.get(fanId);
            final Optional<Interval> timeInterval = fan.getInterval().intersection(brand.getInterval());

            if (timeInterval.isPresent()) {
                int totalNumberOfIntervals = Utils.randomInteger(1, maxFansIntervals + 1);
                List<Interval> intervalList = Interval.createRandomDisjointIntervals(timeInterval.get(), totalNumberOfIntervals);
                Relationship fanRelationship = new Relationship(RelationshipType.FAN, fan, brand, intervalList);
                fans.add(fanRelationship);
            } else {
                logger.error("Error creating fan edge: empty interval. Skipping.");
            }
        }
    }

    private List<Integer> randomFans() {
        int numberOfFans = Utils.randomInteger(1, maxFans + 1);
        List<Integer> personIds = new ArrayList<>(persons.keySet());
        Collections.shuffle(personIds);
        return personIds.subList(0, numberOfFans);
    }

    private Person createPerson(Interval t) {
        final int personId = getNextId();
        Person person = new Person(personId, t);
        persons.put(personId, person);
        createAttribute("Name", person, t);
        return person;
    }

    private Person createPerson2(Interval t) {
        final int personId = getNextId();
        Person person = new Person(personId, t);
        persons2.put(personId, person);
        createAttribute("Name", person, t);
        return person; 
    }

    private void createBrand() {
        final Interval t = new Interval(Interval.MAX_SECONDS_FROM_EPOCH, Granularity.DATE);
        final int brandId = getNextId();
        Brand brand = new Brand(brandId, t);
        createAttribute("Name", brand, t);
        brands.put(brandId, brand);
    }

    private void createCity() {
        final Interval t = new Interval(0L, Interval.MAX_SECONDS_FROM_EPOCH, Granularity.DATE);
        final int cityId = getNextId();
        City city = new City(cityId, t);
        createAttribute("Name", city, t);
        cities.put(cityId, city);
    }

    private void createStars() {
        
        for (int i = 0; i < starCount; i++) {
            final Interval t = new Interval(Interval.MAX_SECONDS_FROM_EPOCH, Granularity.DATE);
            // if (maxPersonStartSeconds < t.getStart()) {
            //     maxPersonStartSeconds = t.getStart();
            // }
            Person p = createPerson(t);
            stars.put(p.getId(), p);
            List<Integer> followingIds = new ArrayList<>(persons.keySet());
            Collections.shuffle(followingIds);

            Integer max = ThreadLocalRandom.current().nextInt(minStarFollowerCount, personCount - generatedNodes(this.cPathGenerationConfigurations) - starCount);
            
            logger.info("{} Creating new star with id {} and {} followers", minStarFollowerCount, p.getId(), max);

            followingIds = followingIds.subList(0, max);
            for (Integer id : followingIds) {
                createFriendship(persons.get(id), p, Collections.singletonList(new Interval(Interval.MAX_SECONDS_FROM_EPOCH, Granularity.DATE)));
            }
        }
    }

    public int getMaxFriendshipIntervals() {
        return maxFriendshipIntervals;
    }

    public int getPersonCount() {
        return personCount;
    }

    public int getMaxFriendships() {
        return maxFriendships;
    }

    public int getCityCount() {
        return cityCount;
    }


    public int getBrandCount() {
        return brandCount;
    }

    public int getMaxFans() {
        return maxFans;
    }

    public int getMaxFansIntervals() {
        return maxFansIntervals;
    }

    public List<CPathGenerationConfiguration> getCPathGenerationConfigurations() {
        return cPathGenerationConfigurations;
    }
}
