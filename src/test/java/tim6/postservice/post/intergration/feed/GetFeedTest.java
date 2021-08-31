package tim6.postservice.post.intergration.feed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static tim6.postservice.helpers.AuthHelper.createAuthToken;

import com.github.javafaker.Faker;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import tim6.postservice.adapter.http.dto.PostGetDTO;
import tim6.postservice.common.CommonTestBase;
import tim6.postservice.domain.models.Post;
import tim6.postservice.domain.models.User;
import tim6.postservice.domain.models.UserInfo;
import tim6.postservice.domain.repositories.PostRepository;
import tim6.postservice.domain.repositories.UserRepository;

public class GetFeedTest extends CommonTestBase {

    private final String USER_ID = UUID.randomUUID().toString();
    private final String FOLLOWED_USER_1 = UUID.randomUUID().toString();
    private final String FOLLOWED_USER_2 = UUID.randomUUID().toString();
    private final String FOLLOWED_USER_3 = UUID.randomUUID().toString();
    private final String PUBLIC_POSTER_ID_1 = UUID.randomUUID().toString();
    private final String PUBLIC_POSTER_ID_2 = UUID.randomUUID().toString();

    @Value("${jwt.secret}")
    private String JWT_SECRET;

    @Autowired private PostRepository postRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TestRestTemplate testRestTemplate;
    @LocalServerPort private int port;

    @Before
    public void initializeData() {
        this.initializeUsers();
        this.initializePosts();
    }

    @After
    public void cleanUpData() {
        this.userRepository.deleteAll();
        this.postRepository.deleteAll();
    }

    private void initializeUsers() {
        final User followed1 =
                new User(
                        this.FOLLOWED_USER_1,
                        "userName",
                        "userAvatar",
                        true,
                        new HashSet<>(),
                        new HashSet<>(),
                        new HashSet<>());
        this.userRepository.save(followed1);

        final User followed2 =
                new User(
                        this.FOLLOWED_USER_2,
                        "userName",
                        "userAvatar",
                        true,
                        new HashSet<>(),
                        new HashSet<>(),
                        new HashSet<>());
        this.userRepository.save(followed2);

        final User followed3 =
                new User(
                        this.FOLLOWED_USER_3,
                        "userName",
                        "userAvatar",
                        false,
                        new HashSet<>(),
                        new HashSet<>(),
                        new HashSet<>());
        this.userRepository.save(followed3);

        final User public1 =
                new User(
                        this.PUBLIC_POSTER_ID_1,
                        "userName",
                        "userAvatar",
                        true,
                        new HashSet<>(),
                        new HashSet<>(),
                        new HashSet<>());
        this.userRepository.save(public1);

        final User public2 =
                new User(
                        this.PUBLIC_POSTER_ID_2,
                        "userName",
                        "userAvatar",
                        true,
                        new HashSet<>(),
                        new HashSet<>(),
                        new HashSet<>());
        this.userRepository.save(public2);

        final User user =
                new User(
                        this.USER_ID,
                        "userName",
                        "userAvatar",
                        true,
                        Set.of(this.FOLLOWED_USER_1, this.FOLLOWED_USER_2, this.FOLLOWED_USER_3),
                        new HashSet<>(),
                        new HashSet<>());
        this.userRepository.save(user);
    }

    private void initializePosts() {
        final Faker faker = Faker.instance();

        final List<Post> postsOfFollower1 = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            postsOfFollower1.add(
                    new Post(
                            UUID.randomUUID().toString(),
                            "imageUrl",
                            "description",
                            new UserInfo(this.FOLLOWED_USER_1, "userName", "userAvatar"),
                            faker.date().past(14, TimeUnit.DAYS),
                            new HashSet<>(),
                            new HashSet<>()));
        }

        final List<Post> postsOfFollower2 = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            postsOfFollower2.add(
                    new Post(
                            UUID.randomUUID().toString(),
                            "imageUrl",
                            "description",
                            new UserInfo(this.FOLLOWED_USER_2, "userName", "userAvatar"),
                            faker.date().past(14, TimeUnit.DAYS),
                            new HashSet<>(),
                            new HashSet<>()));
        }

        final List<Post> postsOfFollower3 = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            postsOfFollower1.add(
                    new Post(
                            UUID.randomUUID().toString(),
                            "imageUrl",
                            "description",
                            new UserInfo(this.FOLLOWED_USER_3, "userName", "userAvatar"),
                            faker.date().past(14, TimeUnit.DAYS),
                            new HashSet<>(),
                            new HashSet<>()));
        }

        final List<Post> postsOfPublic1 = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            postsOfFollower1.add(
                    new Post(
                            UUID.randomUUID().toString(),
                            "imageUrl",
                            "description",
                            new UserInfo(this.PUBLIC_POSTER_ID_1, "userName", "userAvatar"),
                            faker.date().past(14, TimeUnit.DAYS),
                            new HashSet<>(),
                            new HashSet<>()));
        }

        final List<Post> postsOfPublic2 = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            postsOfFollower1.add(
                    new Post(
                            UUID.randomUUID().toString(),
                            "imageUrl",
                            "description",
                            new UserInfo(this.PUBLIC_POSTER_ID_2, "userName", "userAvatar"),
                            faker.date().past(14, TimeUnit.DAYS),
                            new HashSet<>(),
                            new HashSet<>()));
        }

        final List<Post> postsToSave =
                Stream.of(
                                postsOfFollower1,
                                postsOfFollower2,
                                postsOfFollower3,
                                postsOfPublic1,
                                postsOfPublic2)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
        this.postRepository.saveAll(postsToSave);
    }

    @Test
    public void testGetFeed() {
        final URI feedURI = this.buildFeedUri();
        final ResponseEntity<List<PostGetDTO>> response =
                this.sendGetPostsFeedRequest(
                        feedURI, createAuthToken(this.USER_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.OK, response.getStatusCode());

        final List<PostGetDTO> postsFromFeed = response.getBody();
        assertNotNull(postsFromFeed);
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(this.checkIfPostsAreSortedByTimeDescending(postsFromFeed)).isTrue();
        softly.assertThat(
                        this.checkIfPostsAreFromFollowedPosters(
                                postsFromFeed,
                                this.FOLLOWED_USER_1,
                                this.FOLLOWED_USER_2,
                                this.FOLLOWED_USER_3))
                .isTrue();
        softly.assertAll();
    }

    @Test
    public void testGetFeedNoAuth() {
        final URI feedURI = this.buildFeedUri();
        final ResponseEntity<Void> response = this.sendGetPostsFeedRequest(feedURI);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    private URI buildFeedUri() {
        final UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(
                        String.format("http://localhost:%d/api/v1/posts/feed", this.port));
        return builder.build().encode().toUri();
    }

    private ResponseEntity<Void> sendGetPostsFeedRequest(final URI apiEndpoint) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_LENGTH, "0");
        final HttpEntity<Void> entity = new HttpEntity<>(null, headers);

        return this.testRestTemplate.exchange(apiEndpoint, HttpMethod.DELETE, entity, Void.class);
    }

    private ResponseEntity<List<PostGetDTO>> sendGetPostsFeedRequest(
            final URI apiEndpoint, final String token) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Bearer %s", token));
        headers.set(HttpHeaders.CONTENT_LENGTH, "0");
        final HttpEntity<Void> entity = new HttpEntity<>(null, headers);

        return this.testRestTemplate.exchange(
                apiEndpoint, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});
    }

    private boolean checkIfPostsAreSortedByTimeDescending(final List<PostGetDTO> posts) {
        for (int i = 0; i < posts.size() - 1; i++) {
            final PostGetDTO currentPost = posts.get(i);
            final PostGetDTO nextPost = posts.get(i + 1);
            if (currentPost.getPostDate().before(nextPost.getPostDate())) {
                return false;
            }
        }
        return true;
    }

    private boolean checkIfPostsAreFromFollowedPosters(
            final List<PostGetDTO> posts, final String... followed) {
        return posts.stream()
                .allMatch(
                        post ->
                                Arrays.stream(followed)
                                        .anyMatch(
                                                userId -> userId.equals(post.getPoster().getId())));
    }
}
