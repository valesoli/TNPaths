package ar.edu.itba.client;

import ar.edu.itba.population.Population;
import ar.edu.itba.population.models.cpath.CPathGenerationConfiguration;
import ar.edu.itba.population.models.socialnetwork.SocialNetworkBuilder;
import org.apache.commons.configuration2.PropertiesConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SocialNetworkConfigurationParser implements PopulationConfigurationParser {

    private int persons = 10;
    private int maxFriendships = 5;
    private int maxFriendshipIntervals = 2;
    private int cityCount = 5;
    private int brandCount = 0;
    private int maxFans = 2;
    private int maxFansIntervals = 1;
    protected int starCount = 3;
    protected int minStarFollowerCount = 8;
    private List<CPathGenerationConfiguration> cPathConfigurations;

    @Override
    public Population parse(PropertiesConfiguration configuration) {
        persons = configuration.getInt("socialNetwork.persons", persons);
        maxFriendships = configuration.getInt("socialNetwork.maxFriendships", maxFriendships);
        maxFriendshipIntervals = configuration.getInt("socialNetwork.maxFriendshipIntervals", maxFriendshipIntervals);
        cityCount = configuration.getInt("socialNetwork.cityCount", cityCount);
        brandCount = configuration.getInt("socialNetwork.brandCount", brandCount);
        maxFans = configuration.getInt("socialNetwork.maxFans", maxFans);
        maxFansIntervals = configuration.getInt("socialNetwork.maxFansIntervals", maxFansIntervals);
        starCount = configuration.getInt("socialNetwork.starCount", starCount);
        minStarFollowerCount = configuration.getInt("socialNetwork.minStarFollowerCount", minStarFollowerCount);
        List<Integer> cPathNumberOfPaths = configuration.getList(Integer.class, "socialNetwork.cPath.numberOfPaths", new ArrayList<>());
        List<Integer> cPathMinLength = configuration.getList(Integer.class, "socialNetwork.cPath.minLength", new ArrayList<>());

        if (cPathNumberOfPaths.size() != cPathMinLength.size()) {
            throw new IllegalArgumentException("CPath: Number of paths must equal min length");
        }

        cPathConfigurations = createCPathConfigurations(cPathNumberOfPaths, cPathMinLength);

        return build();
    }

    private List<CPathGenerationConfiguration> createCPathConfigurations(List<Integer> cPathNumberOfPaths,
                                                                         List<Integer> cPathMinLength) {
        return IntStream.range(0, cPathNumberOfPaths.size())
                .mapToObj(i -> new CPathGenerationConfiguration(cPathNumberOfPaths.get(i), cPathMinLength.get(i)))
                .collect(Collectors.toList());
    }

    private Population build() {
        return new SocialNetworkBuilder()
                .personCount(persons)
                .maxFriendships(maxFriendships)
                .maxFriendshipIntervals(maxFriendshipIntervals)
                .brandCount(brandCount)
                .maxFans(maxFans)
                .maxFansIntervals(maxFansIntervals)
                .cityCount(cityCount)
                .cPathGenerationConfigurations(cPathConfigurations)
                .starCount(starCount)
                .minStarFollowerCount(minStarFollowerCount)
                .build();
    }
}
