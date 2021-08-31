package tim6.postservice.domain.services;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import tim6.postservice.adapter.kafka.configuration.ProducerService;
import tim6.postservice.domain.exceptions.AuthorizationException;
import tim6.postservice.domain.exceptions.EntityAlreadyExistsException;
import tim6.postservice.domain.exceptions.EntityNotFoundException;
import tim6.postservice.domain.models.Comment;
import tim6.postservice.domain.models.Post;
import tim6.postservice.domain.models.User;
import tim6.postservice.domain.models.UserInfo;
import tim6.postservice.domain.repositories.PostRepository;

@Service
public class PostService {

    final PostRepository postRepository;

    final ElasticsearchOperations elasticsearchOperations;

    final UserService userService;

    final ProducerService producerService;

    private final int MAX_POSTS_FOR_FEED = 20;

    @Autowired
    public PostService(
            final PostRepository postRepository,
            final ElasticsearchOperations elasticsearchOperations,
            final UserService userService,
            final ProducerService producerService) {
        this.postRepository = postRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.userService = userService;
        this.producerService = producerService;
    }

    private static BoolQueryBuilder postsByTagsFieldQuery(final List<String> tags) {
        final BoolQueryBuilder tagsBoolQuery = QueryBuilders.boolQuery();
        for (final String tag : tags) {
            tagsBoolQuery.should(QueryBuilders.matchQuery("tags", tag));
        }
        return tagsBoolQuery;
    }

    private static BoolQueryBuilder postsByPosterIdQuery(final String posterId) {
        final BoolQueryBuilder posterIdBoolQuery = QueryBuilders.boolQuery();
        posterIdBoolQuery.filter(QueryBuilders.termQuery("posterId", posterId));
        return posterIdBoolQuery;
    }

    @NotNull
    private static NativeSearchQuery overviewQuery(
            final Pageable pageable, final BoolQueryBuilder finalizedQuery) {
        return new NativeSearchQueryBuilder()
                .withFields(
                        "id",
                        "imageUrl",
                        "description",
                        "poster",
                        "postDate",
                        "tags",
                        "userTags",
                        "likes",
                        "dislikes",
                        "favorites",
                        "comments")
                .withQuery(finalizedQuery)
                .withSort(SortBuilders.fieldSort("postDate"))
                .withPageable(pageable)
                .build();
    }

    @NotNull
    private static NativeSearchQuery overviewQuery(final BoolQueryBuilder finalizedQuery) {
        return new NativeSearchQueryBuilder()
                .withFields(
                        "id",
                        "imageUrl",
                        "description",
                        "poster",
                        "postDate",
                        "tags",
                        "userTags",
                        "likes",
                        "dislikes",
                        "favorites",
                        "comments")
                .withQuery(finalizedQuery)
                .withSort(SortBuilders.fieldSort("postDate"))
                .build();
    }

    private static void addToLikes(final String authedUserId, final Post post) {
        post.getLikes().add(authedUserId);
    }

    private static void removeFromLikes(final String authedUserId, final Post post) {
        post.getLikes().remove(authedUserId);
    }

    private static void addToDislikes(final String authedUserId, final Post post) {
        post.getDislikes().add(authedUserId);
    }

    private static void removeFromDislikes(final String authedUserId, final Post post) {
        post.getDislikes().remove(authedUserId);
    }

    private static void addToFavorites(final String authedUserId, final Post post) {
        post.getFavorites().add(authedUserId);
    }

    private static void removeFromFavorites(final String authedUserId, final Post post) {
        post.getFavorites().remove(authedUserId);
    }

    public List<Post> getFeed(final String authedUserId) {
        final User authedUser = this.userService.findById(authedUserId);
        final Set<String> followerIds = authedUser.getFollowing();
        final Set<String> mutedUserIds = authedUser.getMuted();
        final List<String> notMutedFollowers =
                followerIds.stream()
                        .filter(id -> !mutedUserIds.contains(id))
                        .collect(Collectors.toList());

        final List<Query> getFollowerPostsQueries =
                notMutedFollowers.stream()
                        .map(id -> PostService.overviewQuery(postsByPosterIdQuery(id)))
                        .collect(Collectors.toList());

        final List<SearchHits<Post>> searchHits =
                this.elasticsearchOperations.multiSearch(getFollowerPostsQueries, Post.class);

        final List<Post> sortedPosts =
                searchHits.stream()
                        .flatMap(SearchHits::stream)
                        .map(SearchHit::getContent)
                        .sorted(Comparator.comparing(Post::getPostDate).reversed())
                        .collect(Collectors.toList());

        return sortedPosts.size() < this.MAX_POSTS_FOR_FEED
                ? sortedPosts
                : sortedPosts.subList(0, this.MAX_POSTS_FOR_FEED + 1);
    }

    private BoolQueryBuilder postsMustBeVisibleAndPosterNotMutedByAuthedUserQuery(
            final String authedUserId) {
        final List<String> allowedPosterIds =
                this.userService.getViewableUnmutedPosterIdsForUser(authedUserId);

        final BoolQueryBuilder visiblePostsBoolQuery = QueryBuilders.boolQuery();
        for (final String allowedPosterId : allowedPosterIds) {
            visiblePostsBoolQuery.should(QueryBuilders.termQuery("posterId", allowedPosterId));
        }
        return visiblePostsBoolQuery;
    }

    private boolean isPosterContentVisibleToUser(
            final String posterIdToCheck, final String userId) {
        final List<String> allowedPosterIds = this.userService.getViewablePosterIdsForUser(userId);
        return allowedPosterIds.stream().anyMatch(posterIdToCheck::equals);
    }

    public Post createNew(final Post post) {
        if (this.postRepository.findById(post.getId()).isPresent()) {
            throw new EntityAlreadyExistsException();
        } else {
            final User poster = this.userService.findById(post.getPoster().getId());
            post.setPoster(new UserInfo(poster));

            final Collection<User> usersByIds =
                    this.userService.findAllByUsernames(
                            post.getUserTags().stream()
                                    .map(UserInfo::getUsername)
                                    .collect(Collectors.toList()));

            final Set<UserInfo> taggedUsers =
                    usersByIds.stream().map(UserInfo::new).collect(Collectors.toSet());
            post.setUserTags(taggedUsers);

            final Post createdPost = this.postRepository.save(post);

            if (post.getTags().size() > 0) {
                this.producerService.sendMessage(poster.getId(), createdPost.getTags());
            }
            return createdPost;
        }
    }

    public Post save(final Post post) {
        return this.postRepository.save(post);
    }

    public Post getPostById(final String id, final String authedUserId) {
        final Post post =
                this.postRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        if (this.isPosterContentVisibleToUser(post.getPoster().getId(), authedUserId)) {
            return post;
        } else {
            throw new AuthorizationException();
        }
    }

    public SearchPage<Post> postsByPosterIdQuery(
            final String posterId, final Pageable pageable, final String authedUserId) {
        if (this.isPosterContentVisibleToUser(posterId, authedUserId)) {
            final BoolQueryBuilder finalizedQuery = QueryBuilders.boolQuery();
            finalizedQuery.must(PostService.postsByPosterIdQuery(posterId));

            final NativeSearchQuery searchQuery =
                    PostService.overviewQuery(pageable, finalizedQuery);
            final SearchHits<Post> tagsHits =
                    this.elasticsearchOperations.search(searchQuery, Post.class);
            return SearchHitSupport.searchPageFor(tagsHits, searchQuery.getPageable());
        } else {
            throw new AuthorizationException();
        }
    }

    public SearchPage<Post> getPostsByTags(
            final List<String> tags, final Pageable pageable, final String authedUserId) {
        final BoolQueryBuilder finalizedQuery = QueryBuilders.boolQuery();
        finalizedQuery.must(
                this.postsMustBeVisibleAndPosterNotMutedByAuthedUserQuery(authedUserId));
        finalizedQuery.must(PostService.postsByTagsFieldQuery(tags));
        final NativeSearchQuery postsByTagsQuery =
                PostService.overviewQuery(pageable, finalizedQuery);

        final SearchHits<Post> tagsHits =
                this.elasticsearchOperations.search(postsByTagsQuery, Post.class);
        return SearchHitSupport.searchPageFor(tagsHits, postsByTagsQuery.getPageable());
    }

    public void likePost(final String postId, final String authedUserId) {
        if (authedUserId.equals("anonymousUser")) {
            throw new AuthorizationException();
        }

        final Post post = this.getPostById(postId, authedUserId);
        PostService.removeFromDislikes(authedUserId, post);
        PostService.addToLikes(authedUserId, post);

        this.postRepository.save(post);
    }

    public void removeLikeFromPost(final String postId, final String authedUserId) {
        if (authedUserId.equals("anonymousUser")) {
            throw new AuthorizationException();
        }

        final Post post = this.getPostById(postId, authedUserId);
        PostService.removeFromLikes(authedUserId, post);

        this.postRepository.save(post);
    }

    public void dislikePost(final String postId, final String authedUserId) {
        if (authedUserId.equals("anonymousUser")) {
            throw new AuthorizationException();
        }

        final Post post = this.getPostById(postId, authedUserId);
        PostService.removeFromLikes(authedUserId, post);
        PostService.addToDislikes(authedUserId, post);

        this.postRepository.save(post);
    }

    public void removeDislikeFromPost(final String postId, final String authedUserId) {
        if (authedUserId.equals("anonymousUser")) {
            throw new AuthorizationException();
        }

        final Post post = this.getPostById(postId, authedUserId);
        PostService.removeFromDislikes(authedUserId, post);

        this.postRepository.save(post);
    }

    public void favoritePost(final String postId, final String authedUserId) {
        if (authedUserId.equals("anonymousUser")) {
            throw new AuthorizationException();
        }

        final Post post = this.getPostById(postId, authedUserId);
        PostService.addToFavorites(authedUserId, post);

        this.postRepository.save(post);
    }

    public void unfavoritePost(final String postId, final String authedUserId) {
        if (authedUserId.equals("anonymousUser")) {
            throw new AuthorizationException();
        }

        final Post post = this.getPostById(postId, authedUserId);
        PostService.removeFromFavorites(authedUserId, post);

        this.postRepository.save(post);
    }

    public void updateUserInfoInRelevantDocuments(final UserInfo userInfo) {
        Collection<Post> postsByUser = this.postRepository.findAllByPosterId(userInfo.getId());
        postsByUser.forEach(post -> post.setPoster(userInfo));
        this.postRepository.saveAll(postsByUser);

        final Collection<Post> postsThatMentionUser =
                this.getPostsMentioningUserInTagsOrComments(userInfo);
        postsThatMentionUser.forEach(
                post -> {
                    this.updateUserInfoInUserTags(userInfo, post);
                    this.updateUserInfoInComments(userInfo, post);
                });
        this.postRepository.saveAll(postsThatMentionUser);
    }

    @NotNull
    private Collection<Post> getPostsMentioningUserInTagsOrComments(UserInfo userInfo) {
        final BoolQueryBuilder userMentioned = QueryBuilders.boolQuery();
        userMentioned.should(QueryBuilders.termQuery("userTags.id", userInfo.getId()));
        userMentioned.should(QueryBuilders.termQuery("comments.commenter.id", userInfo.getId()));
        final NativeSearchQuery userMentionedQuery = new NativeSearchQuery(userMentioned);
        final SearchHits<Post> searchHits =
                this.elasticsearchOperations.search(userMentionedQuery, Post.class);

        final Collection<Post> postsThatMentionUser =
                searchHits.getSearchHits().stream()
                        .map(SearchHit::getContent)
                        .collect(Collectors.toList());
        return postsThatMentionUser;
    }

    private void updateUserInfoInComments(UserInfo userInfo, Post post) {
        for (int i = 0; i < post.getComments().size(); i++) {
            final Comment comment = post.getComments().get(i);
            if (comment.getCommenter().equals(userInfo)) {
                comment.setCommenter(userInfo);
            }
        }
    }

    private void updateUserInfoInUserTags(UserInfo userInfo, Post post) {
        if (post.getUserTags().contains(userInfo)) {
            post.getUserTags().remove(userInfo);
            post.getUserTags().add(userInfo);
        }
    }
}
