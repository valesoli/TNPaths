package ar.edu.itba.population.models.socialnetwork;

import ar.edu.itba.population.models.cpath.CPathGenerationConfiguration;

import java.util.List;

public class SocialNetworkBuilder {

    protected int personCount = 10;
    protected int maxFriendships = 3;
    protected int maxFriendshipIntervals = 2;
    protected int brandCount = 3;
    protected int maxFans = 2;
    protected int maxFansIntervals = 1;
    protected int cityCount = 5;
    protected int starCount = 3;
    protected int minStarFollowerCount = 8;
    protected List<CPathGenerationConfiguration> cPathConfigurations;

    public SocialNetworkBuilder personCount(int val) {
        personCount = val;
        return this;
    }

    public SocialNetworkBuilder maxFriendships(int val) {
        maxFriendships = val;
        return this;
    }

    public SocialNetworkBuilder maxFriendshipIntervals(int val) {
        maxFriendshipIntervals = val;
        return this;
    }

    public SocialNetworkBuilder brandCount(int val) {
        brandCount = val;
        return this;
    }

    public SocialNetworkBuilder maxFans(int val) {
        maxFans = val;
        return this;
    }

    public SocialNetworkBuilder maxFansIntervals(int val) {
        maxFansIntervals = val;
        return this;
    }

    public SocialNetworkBuilder cityCount(int val){
        this.cityCount = val;
        return this;
    }

    public SocialNetworkBuilder cPathGenerationConfigurations(List<CPathGenerationConfiguration> val) {
        this.cPathConfigurations = val;
        return this;
    }

    public SocialNetworkBuilder starCount(int val) {
        this.starCount = val;
        return this;
    }
 
    public SocialNetworkBuilder minStarFollowerCount(int val) {
        this.minStarFollowerCount = val;
        return this;
    }


    public SocialNetwork build() {

        if (maxFriendships >= personCount) {
            throw new IllegalArgumentException("Maximum friendships can not be greater or equal than population size.");
        }

        int generatedNodes = SocialNetwork.generatedNodes(cPathConfigurations);

        if (personCount < generatedNodes) {
            throw new IllegalArgumentException("CPath generated nodes can not be greater than population size.");
        }

        return new SocialNetwork(this);
    }
}
