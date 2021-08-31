package tim6.postservice.domain.services;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import tim6.postservice.domain.exceptions.EntityNotFoundException;
import tim6.postservice.domain.models.User;
import tim6.postservice.domain.repositories.UserRepository;

@Service
public class UserService {

    final UserRepository userRepository;

    final ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public UserService(
            final UserRepository userRepository,
            final ElasticsearchOperations elasticsearchOperations) {
        this.userRepository = userRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    private static BoolQueryBuilder publicAccountQuery() {
        final BoolQueryBuilder publicAccountQuery = QueryBuilders.boolQuery();
        publicAccountQuery.must(QueryBuilders.termQuery("publicAccount", true));
        return publicAccountQuery;
    }

    private static List<String> removeNecessaryUserIdsFrom(
            final List<String> userIds, final Collection<String> blockedUserIds) {
        return userIds.stream()
                .filter(id -> !blockedUserIds.contains(id))
                .collect(Collectors.toList());
    }

    public User findById(final String userId) {
        return this.userRepository.findById(userId).orElseThrow(EntityNotFoundException::new);
    }

    public Collection<User> findByIds(final Collection<String> userIds) {
        return this.userRepository.getAllByIdIn(userIds);
    }

    public Collection<User> findAllByUsernames(final Collection<String> userNames) {
        return this.userRepository.getAllByUsernameIn(userNames);
    }

    public void followUser(final String userId, final String userToFollowId) {
        final User user = this.findById(userId);
        user.getFollowing().add(userToFollowId);
        this.save(user);
    }

    public void unfollowUser(final String userId, final String userToFollowId) {
        final User user = this.findById(userId);
        user.getFollowing().remove(userToFollowId);
        this.save(user);
    }

    public void muteUser(final String userId, final String userToFollowId) {
        final User user = this.findById(userId);
        user.getMuted().add(userToFollowId);
        this.save(user);
    }

    public void unmuteUser(final String userId, final String userToFollowId) {
        final User user = this.findById(userId);
        user.getMuted().remove(userToFollowId);
        this.save(user);
    }

    public void blockUser(final String userId, final String userToFollowId) {
        final User user = this.findById(userId);
        user.getBlocked().add(userToFollowId);
        this.save(user);
    }

    public void unblockUser(final String userId, final String userToFollowId) {
        final User user = this.findById(userId);
        user.getBlocked().remove(userToFollowId);
        this.save(user);
    }

    public User save(final User user) {
        return this.userRepository.save(user);
    }

    public List<String> getViewablePosterIdsForUser(final String userId) {
        final List<String> publicPosterIds = this.getPublicPosters();
        if (userId.equals("anonymousUser")) {
            return publicPosterIds;
        }

        final User user = this.findById(userId);
        final Set<String> followedUserIds = user.getFollowing();

        final List<String> combinedViewablePosterIds =
                Stream.concat(publicPosterIds.stream(), followedUserIds.stream())
                        .distinct()
                        .collect(Collectors.toList());

        final Set<String> blockedUserIds = user.getBlocked();
        return removeNecessaryUserIdsFrom(combinedViewablePosterIds, blockedUserIds);
    }

    public List<String> getViewableUnmutedPosterIdsForUser(final String userId) {
        final List<String> viewablePosterIds = this.getViewablePosterIdsForUser(userId);
        if (userId.equals("anonymousUser")) {
            return viewablePosterIds;
        }

        final User user = this.findById(userId);
        final Set<String> mutedUserIds = user.getMuted();

        return removeNecessaryUserIdsFrom(viewablePosterIds, mutedUserIds);
    }

    private List<String> getPublicPosters() {
        final BoolQueryBuilder viewablePosterIdsQuery = QueryBuilders.boolQuery();
        viewablePosterIdsQuery.should(publicAccountQuery());

        final NativeSearchQuery getViewablePosterIdsQuery =
                new NativeSearchQueryBuilder()
                        .withFields("id")
                        .withFilter(viewablePosterIdsQuery)
                        .build();
        final SearchHits<User> queryHits =
                this.elasticsearchOperations.search(getViewablePosterIdsQuery, User.class);

        return queryHits.stream().map(hit -> hit.getContent().getId()).collect(Collectors.toList());
    }
}
